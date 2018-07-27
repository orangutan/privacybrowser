/*
 * Copyright © 2018 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser <https://www.stoutner.com/privacy-browser>.
 *
 * Privacy Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Browser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stoutner.privacybrowser.helpers;

import android.content.res.AssetManager;

import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BlockListHelper {
    public ArrayList<List<String[]>> parseBlockList(AssetManager assets, String blockListName) {
        // Initialize the header list.
        List<String[]> headers = new ArrayList<>();  // 0.

        // Initialize the whitelists.
        List<String[]> mainWhiteList = new ArrayList<>();  // 1.
        List<String[]> finalWhiteList = new ArrayList<>();  // 2.
        List<String[]> domainWhiteList = new ArrayList<>();  // 3.
        List<String[]> domainInitialWhiteList = new ArrayList<>();  // 4.
        List<String[]> domainFinalWhiteList = new ArrayList<>();  // 5.
        List<String[]> thirdPartyWhiteList = new ArrayList<>();  // 6.
        List<String[]> thirdPartyDomainWhiteList = new ArrayList<>();  // 7.
        List<String[]> thirdPartyDomainInitialWhiteList = new ArrayList<>();  // 8.

        // Initialize the blacklists
        List<String[]> mainBlackList = new ArrayList<>();  // 9.
        List<String[]> initialBlackList = new ArrayList<>();  // 10.
        List<String[]> finalBlackList = new ArrayList<>();  // 11.
        List<String[]> domainBlackList = new ArrayList<>();  // 12.
        List<String[]> domainInitialBlackList = new ArrayList<>();  // 13.
        List<String[]> domainFinalBlackList = new ArrayList<>();  // 14.
        List<String[]> domainRegularExpressionBlackList = new ArrayList<>();  // 15.
        List<String[]> thirdPartyBlackList = new ArrayList<>();  // 16.
        List<String[]> thirdPartyInitialBlackList = new ArrayList<>();  // 17.
        List<String[]> thirdPartyDomainBlackList = new ArrayList<>();  // 18.
        List<String[]> thirdPartyDomainInitialBlackList = new ArrayList<>();  // 19.
        List<String[]> regularExpressionBlackList = new ArrayList<>();  // 20.
        List<String[]> thirdPartyRegularExpressionBlackList = new ArrayList<>();  // 21.
        List<String[]> thirdPartyDomainRegularExpressionBlackList = new ArrayList<>();  // 22.


        // Populate the block lists.  The `try` is required by `InputStreamReader`.
        try {
            // Load the block list into a `BufferedReader`.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assets.open(blockListName)));

            // Create a string for storing the block list entries.
            String blockListEntry;

            // Parse the block list.
            while ((blockListEntry = bufferedReader.readLine()) != null) {
                // Store the original block list entry.
                String originalBlockListEntry = blockListEntry;

                // Remove any `^` from the block list entry.  Privacy Browser does not process them in the interest of efficiency.
                blockListEntry = blockListEntry.replace("^", "");

                //noinspection StatementWithEmptyBody
                if (blockListEntry.contains("##") || blockListEntry.contains("#?#") || blockListEntry.contains("#@#") || blockListEntry.startsWith("[")) {
                    // Entries that contain `##`, `#?#`, and `#@#` are for hiding elements in the main page's HTML.  Entries that start with `[` describe the AdBlock compatibility level.
                    // Do nothing.  Privacy Browser does not currently use these entries.

                    //Log.i("BlockLists", "Not added: " + blockListEntry);
                } else //noinspection StatementWithEmptyBody
                    if (blockListEntry.contains("$csp=script-src")) {  // Ignore entries that contain `$csp=script-src`.
                        // Do nothing.  It is uncertain what this directive is even supposed to mean, and it is blocking entire websites like androidcentral.com.  https://redmine.stoutner.com/issues/306.

                        //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                } else //noinspection StatementWithEmptyBody
                    if (blockListEntry.contains("$websocket") || blockListEntry.contains("$third-party,websocket") || blockListEntry.contains("$script,websocket")) {  // Ignore entries with `websocket`.
                        // Do nothing.  Privacy Browser does not differentiate between websocket requests and other requests and these entries cause a lot of false positivies.

                        //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                } else if (blockListEntry.startsWith("!")) {  //  Comment entries.
                    if (blockListEntry.startsWith("! Version:")) {
                        // Get the list version number.
                        String[] listVersion = {blockListEntry.substring(11)};

                        // Store the list version in the headers list.
                        headers.add(listVersion);
                    }

                    if (blockListEntry.startsWith("! Title:")) {
                        // Get the list title.
                        String[] listTitle = {blockListEntry.substring(9)};

                        // Store the list title in the headers list.
                        headers.add(listTitle);
                    }

                    //Log.i("BlockLists", "Not added: " + blockListEntry);
                } else if (blockListEntry.startsWith("@@")) {  // Entries that begin with `@@` are whitelists.
                    // Remove the `@@`
                    blockListEntry = blockListEntry.substring(2);

                    // Strip out any initial `||`.  Privacy Browser doesn't differentiate items that only match against the end of the domain name.
                    if (blockListEntry.startsWith("||")) {
                        blockListEntry = blockListEntry.substring(2);
                    }

                    if (blockListEntry.contains("$")) {  // Filter entries.
                        //noinspection StatementWithEmptyBody
                        if (blockListEntry.contains("~third-party")) {  // Ignore entries that contain `~third-party`.
                            // Do nothing.

                            //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                        } else if (blockListEntry.contains("third-party")) {  // Third-party white list entries.
                            if (blockListEntry.contains("domain=")) {  // Third-party domain white list entries.
                                // Parse the entry.
                                String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                                String filters = blockListEntry.substring(blockListEntry.indexOf("$") + 1);
                                String domains = filters.substring(filters.indexOf("domain=") + 7);

                                //noinspection StatementWithEmptyBody
                                if (domains.contains("~")) {  // It is uncertain what a `~` domain means inside an `@@` entry.
                                    // Do Nothing

                                    //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                                } else if (blockListEntry.startsWith("|")) {  // Third-party domain initial white list entries.
                                    // Strip out the initial `|`.
                                    entry = entry.substring(1);

                                    //noinspection StatementWithEmptyBody
                                    if (entry.equals("http://") || entry.equals("https://")) {  // Ignore generic entries.
                                        // Do nothing.  These entries are designed for filter options that Privacy Browser does not use.

                                        //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                                    } else {  // Process third-party domain initial white list entries.
                                        // Process each domain.
                                        do {
                                            // Create a string to keep track of the current domain.
                                            String domain;

                                            if (domains.contains("|")) {  // There is more than one domain in the list.
                                                // Get the first domain from the list.
                                                domain = domains.substring(0, domains.indexOf("|"));

                                                // Remove the first domain from the list.
                                                domains = domains.substring(domains.indexOf("|") + 1);
                                            } else {  // There is only one domain in the list.
                                                domain = domains;
                                            }

                                            if (entry.contains("*")) {  // Process a third-party domain initial white list double entry.
                                                // Get the index of the wildcard.
                                                int wildcardIndex = entry.indexOf("*");

                                                // Split the entry into components.
                                                String firstEntry = entry.substring(0, wildcardIndex);
                                                String secondEntry = entry.substring(wildcardIndex + 1);

                                                // Create an entry string array.
                                                String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                                // Add the entry to the white list.
                                                thirdPartyDomainInitialWhiteList.add(domainDoubleEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " third-party domain initial white list added: " + domain + " , " + firstEntry + " , " + secondEntry +
                                                //        "  -  " + originalBlockListEntry);
                                            } else {  // Process a third-party domain initial white list single entry.
                                                // Create a domain entry string array.
                                                String[] domainEntry = {domain, entry, originalBlockListEntry};

                                                // Add the entry to the third party domain initial white list.
                                                thirdPartyDomainInitialWhiteList.add(domainEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " third-party domain initial white list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                            }
                                        } while (domains.contains("|"));
                                    }
                                } else {  // Third-party domain entries.
                                    // Process each domain.
                                    do {
                                        // Create a string to keep track of the current domain.
                                        String domain;

                                        if (domains.contains("|")) {  // three is more than one domain in the list.
                                            // Get the first domain from the list.
                                            domain = domains.substring(0, domains.indexOf("|"));

                                            // Remove the first domain from the list.
                                            domains = domains.substring(domains.indexOf("|") + 1);
                                        } else {  // There is only one domain in the list.
                                            domain = domains;
                                        }

                                        // Remove any trailing `*` from the entry.
                                        if (entry.endsWith("*")) {
                                            entry = entry.substring(0, entry.length() - 1);
                                        }

                                        if (entry.contains("*")) {  // Process a third-party domain double entry.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhiteList.add(domainDoubleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlockListEntry);
                                        } else {  // Process a third-party domain single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlockListEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhiteList.add(domainEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                        }
                                    } while (domains.contains("|"));
                                }
                            } else {  // Process third-party white list entries.
                                // Parse the entry
                                String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));

                                if (entry.contains("*")) {  // There are two or more entries.
                                    // Get the index of the wildcard.
                                    int wildcardIndex = entry.indexOf("*");

                                    // Split the entry into components.
                                    String firstEntry = entry.substring(0, wildcardIndex);
                                    String secondEntry = entry.substring(wildcardIndex + 1);

                                    if (secondEntry.contains("*")) {  // There are three or more entries.
                                        // Get the index of the wildcard.
                                        int secondWildcardIndex = secondEntry.indexOf("*");

                                        // Split the entry into components.
                                        String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                        String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                        if (thirdEntry.contains("*")) {  // There are four or more entries.
                                            // Get the index of the wildcard.
                                            int thirdWildcardIndex = thirdEntry.indexOf("*");

                                            // Split the entry into components.
                                            String realThirdEntry = thirdEntry.substring(0, thirdWildcardIndex);
                                            String fourthEntry = thirdEntry.substring(thirdWildcardIndex + 1);

                                            if (fourthEntry.contains("*")) {  // Process a third-party white list quintuple entry.
                                                // Get the index of the wildcard.
                                                int fourthWildcardIndex = fourthEntry.indexOf("*");

                                                // Split the entry into components.
                                                String realFourthEntry = fourthEntry.substring(0, fourthWildcardIndex);
                                                String fifthEntry = fourthEntry.substring(fourthWildcardIndex + 1);

                                                // Create an entry string array.
                                                String[] quintupleEntry = {firstEntry, realSecondEntry, realThirdEntry, realFourthEntry, fifthEntry, originalBlockListEntry};

                                                // Add the entry to the white list.
                                                thirdPartyWhiteList.add(quintupleEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                                //        realFourthEntry + " , " + fifthEntry + "  -  " + originalBlockListEntry);
                                            } else {  // Process a third-party white list quadruple entry.
                                                // Create an entry string array.
                                                String[] quadrupleEntry = {firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlockListEntry};

                                                // Add the entry to the white list.
                                                thirdPartyWhiteList.add(quadrupleEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                                //        fourthEntry + "  -  " + originalBlockListEntry);
                                            }
                                        } else {  // Process a third-party white list triple entry.
                                            // Create an entry string array.
                                            String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                            // Add the entry to the white list.
                                            thirdPartyWhiteList.add(tripleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                            //        originalBlockListEntry);
                                        }
                                    } else {  // Process a third-party white list double entry.
                                        // Create an entry string array.
                                        String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                        // Add the entry to the white list.
                                        thirdPartyWhiteList.add(doubleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                                    }
                                } else {  // Process a third-party white list single entry.
                                    // Create an entry string array.
                                    String[] singleEntry = {entry, originalBlockListEntry};

                                    // Add the entry to the white list.
                                    thirdPartyWhiteList.add(singleEntry);

                                    //Log.i("BlockLists", headers.get(1)[0] + " third-party domain white list added: " + entry + "  -  " + originalBlockListEntry);
                                }
                            }
                        } else if (blockListEntry.contains("domain=")) {  // Process domain white list entries.
                            // Parse the entry
                            String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                            String filters = blockListEntry.substring(blockListEntry.indexOf("$") + 1);
                            String domains = filters.substring(filters.indexOf("domain=") + 7);

                            if (entry.startsWith("|")) {  // Initial domain white list entries.
                                // Strip the initial `|`.
                                entry = entry.substring(1);

                                //noinspection StatementWithEmptyBody
                                if (entry.equals("http://") || entry.equals("https://")) {  // Ignore generic entries.
                                    // Do nothing.  These entries are designed for filter options that Privacy Browser does not use.

                                    //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                                } else {  // Initial domain white list entry.
                                    // Process each domain.
                                    do {
                                        // Create a string to keep track of the current domain.
                                        String domain;

                                        if (domains.contains("|")) {  // There is more than one domain in the list.
                                            // Get the first domain from the list.
                                            domain = domains.substring(0, domains.indexOf("|"));

                                            // Remove the first domain from the list.
                                            domains = domains.substring(domains.indexOf("|") + 1);
                                        } else {  // There is only one domain in the list.
                                            domain = domains;
                                        }

                                        if (entry.contains("*")) {  // There are two or more entries.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            if (secondEntry.contains("*")) {  // Process a domain initial triple entry.
                                                // Get the index of the wildcard.
                                                int secondWildcardIndex = secondEntry.indexOf("*");

                                                // Split the entry into components.
                                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                                // Create an entry string array.
                                                String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                                // Add the entry to the white list.
                                                domainInitialWhiteList.add(domainTripleEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " domain initial white list entry added: " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " +
                                                //        thirdEntry + "  -  " + originalBlockListEntry);
                                            } else {  // Process a domain initial double entry.
                                                // Create an entry string array.
                                                String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                                // Add the entry to the white list.
                                                domainInitialWhiteList.add(domainDoubleEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " domain initial white list entry added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                                //        originalBlockListEntry);
                                            }
                                        } else {  // Process a domain initial single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlockListEntry};

                                            // Add the entry to the white list.
                                            domainInitialWhiteList.add(domainEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " domain initial white list entry added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                        }
                                    } while (domains.contains("|"));
                                }
                            } else if (entry.endsWith("|")) {  // Final domain white list entries.
                                // Strip the `|` from the end of the entry.
                                entry = entry.substring(0, entry.length() - 1);

                                // Process each domain.
                                do {
                                    // Create a string to keep track of the current domain.
                                    String domain;

                                    if (domains.contains("|")) {  // There is more than one domain in the list.
                                        // Get the first domain from the list.
                                        domain = domains.substring(0, domains.indexOf("|"));

                                        // Remove the first domain from the list.
                                        domains = domains.substring(domains.indexOf("|") + 1);
                                    } else {  // There is only one domain in the list.
                                        domain = domains;
                                    }

                                    if (entry.contains("*")) {  // Process a domain final white list double entry.
                                        // Get the index of the wildcard.
                                        int wildcardIndex = entry.indexOf("*");

                                        // Split the entry into components.
                                        String firstEntry = entry.substring(0, wildcardIndex);
                                        String secondEntry = entry.substring(wildcardIndex + 1);

                                        // Create an entry string array.
                                        String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                        // Add the entry to the white list.
                                        domainFinalWhiteList.add(domainDoubleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " domain final white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                        //        originalBlockListEntry);
                                    } else {  // Process a domain final white list single entry.
                                        // create an entry string array.
                                        String[] domainEntry = {domain, entry, originalBlockListEntry};

                                        // Add the entry to the white list.
                                        domainFinalWhiteList.add(domainEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " domain final white list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                    }
                                } while (domains.contains("|"));

                            } else {  // Standard domain white list entries with filters.
                                //noinspection StatementWithEmptyBody
                                if (domains.contains("~")) {  // It is uncertain what a `~` domain means inside an `@@` entry.
                                    // Do Nothing

                                    //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                                } else {
                                    // Process each domain.
                                    do {
                                        // Create a string to keep track of the current domain.
                                        String domain;

                                        if (domains.contains("|")) {  // There is more than one domain in the list.
                                            // Get the first domain from the list.
                                            domain = domains.substring(0, domains.indexOf("|"));

                                            // Remove the first domain from the list.
                                            domains = domains.substring(domains.indexOf("|") + 1);
                                        } else {  // There is only one domain in the list.
                                            domain = domains;
                                        }

                                        if (entry.contains("*")) {  // There are two or more entries.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            if (secondEntry.contains("*")) {  // There are three or more entries.
                                                // Get the index of the wildcard.
                                                int secondWildcardIndex = secondEntry.indexOf("*");

                                                // Split the entry into components.
                                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                                if (thirdEntry.contains("*")) {  // Process a domain white list quadruple entry.
                                                    // Get the index of the wildcard.
                                                    int thirdWildcardIndex = thirdEntry.indexOf("*");

                                                    // Split the entry into components.
                                                    String realThirdEntry = thirdEntry.substring(0, thirdWildcardIndex);
                                                    String fourthEntry = thirdEntry.substring(thirdWildcardIndex + 1);

                                                    // Create an entry string array.
                                                    String[] domainQuadrupleEntry = {domain, firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlockListEntry};

                                                    // Add the entry to the white list.
                                                    domainWhiteList.add(domainQuadrupleEntry);

                                                    //Log.i("BlockLists", headers.get(1)[0] + " domain white list added : " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " +
                                                    //        realThirdEntry + " , " + fourthEntry + "  -  " + originalBlockListEntry);
                                                } else {  // Process a domain white list triple entry.
                                                    // Create an entry string array.
                                                    String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                                    // Add the entry to the white list.
                                                    domainWhiteList.add(domainTripleEntry);

                                                    //Log.i("BlockLists", headers.get(1)[0] + " domain white list added : " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " +
                                                    //        thirdEntry + "  -  " + originalBlockListEntry);
                                                }
                                            } else {  // Process a domain white list double entry.
                                                // Create an entry string array.
                                                String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                                // Add the entry to the white list.
                                                domainWhiteList.add(domainDoubleEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " domain white list added : " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                                //        originalBlockListEntry);
                                            }
                                        } else {  // Process a domain white list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlockListEntry};

                                            // Add the entry to the white list.
                                            domainWhiteList.add(domainEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " domain white list added : " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                        }
                                    } while (domains.contains("|"));
                                }
                            }
                        }  // Ignore all other filter entries.
                    } else if (blockListEntry.endsWith("|")) {  // Final white list entries.
                        // Remove the final `|` from the entry.
                        String entry = blockListEntry.substring(0, blockListEntry.length() - 1);

                        if (entry.contains("*")) {  // Process a final white list double entry
                            // Get the index of the wildcard.
                            int wildcardIndex = entry.indexOf("*");

                            // split the entry into components.
                            String firstEntry = entry.substring(0, wildcardIndex);
                            String secondEntry = entry.substring(wildcardIndex + 1);

                            // Create an entry string array.
                            String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                            // Add the entry to the white list.
                            finalWhiteList.add(doubleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " final white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                        } else {  // Process a final white list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlockListEntry};

                            // Add the entry to the white list.
                            finalWhiteList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " final white list added: " + entry + "  -  " + originalBlockListEntry);
                        }
                    } else {  // Main white list entries.
                        if (blockListEntry.contains("*")) {  // There are two or more entries.
                            // Get the index of the wildcard.
                            int wildcardIndex = blockListEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blockListEntry.substring(0, wildcardIndex);
                            String secondEntry = blockListEntry.substring(wildcardIndex + 1);

                            if (secondEntry.contains("*")) {  // Process a main white list triple entry.
                                // Get the index of the wildcard.
                                int secondWildcardIndex = secondEntry.indexOf("*");

                                // Split the entry into components.
                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                // Create an entry string array.
                                String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                // Add the entry to the white list.
                                mainWhiteList.add(tripleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " main white list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " + originalBlockListEntry);
                            } else {  // Process a main white list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                // Add the entry to the white list.
                                mainWhiteList.add(doubleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " main white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                            }
                        } else {  // Process a main white list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blockListEntry, originalBlockListEntry};

                            // Add the entry to the white list.
                            mainWhiteList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " main white list added: " + blockListEntry + "  -  " + originalBlockListEntry);
                        }
                    }
                } else if (blockListEntry.endsWith("|")) {  // Final black list entries.
                    // Strip out the final "|"
                    String entry = blockListEntry.substring(0, blockListEntry.length() - 1);

                    // Strip out any initial `||`.  They are redundant in this case because the block list entry is being matched against the end of the URL.
                    if (entry.startsWith("||")) {
                        entry = entry.substring(2);
                    }

                    if (entry.contains("*")) {  // Process a final black list double entry.
                        // Get the index of the wildcard.
                        int wildcardIndex = entry.indexOf("*");

                        // Split the entry into components.
                        String firstEntry = entry.substring(0, wildcardIndex);
                        String secondEntry = entry.substring(wildcardIndex + 1);

                        // Create an entry string array.
                        String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                        // Add the entry to the black list.
                        finalBlackList.add(doubleEntry);

                        //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                    } else {  // Process a final black list single entry.
                        // create an entry string array.
                        String[] singleEntry = {entry, originalBlockListEntry};

                        // Add the entry to the black list.
                        finalBlackList.add(singleEntry);

                        //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + entry + "  -  " + originalBlockListEntry);
                    }
                } else if (blockListEntry.contains("$")) {  // Entries with filter options.
                    // Strip out any initial `||`.  These will be treated like any other entry.
                    if (blockListEntry.startsWith("||")) {
                        blockListEntry = blockListEntry.substring(2);
                    }

                    if (blockListEntry.contains("third-party")) {  // Third-party entries.
                        //noinspection StatementWithEmptyBody
                        if (blockListEntry.contains("~third-party")) {  // Third-party filter white list entries.
                            // Do not process these white list entries.  They are designed to combine with block filters that Privacy Browser doesn't use, like `subdocument` and `xmlhttprequest`.

                            //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                        } else if (blockListEntry.contains("domain=")) {  // Third-party domain entries.
                            if (blockListEntry.startsWith("|")) {  // Third-party domain initial entries.
                                // Strip the initial `|`.
                                blockListEntry = blockListEntry.substring(1);

                                // Parse the entry
                                String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                                String filters = blockListEntry.substring(blockListEntry.indexOf("$") + 1);
                                String domains = filters.substring(filters.indexOf("domain=") + 7);

                                //noinspection StatementWithEmptyBody
                                if (entry.equals("http:") || entry.equals("https:") || entry.equals("http://") || entry.equals("https://")) {  // Ignore generic entries.
                                    // Do nothing.  These entries will almost entirely disable the website.
                                    // Often the original entry blocks filter options like `$script`, which Privacy Browser does not differentiate.

                                    //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                                } else {  // Third-party domain initial entries.
                                    // Process each domain.
                                    do {
                                        // Create a string to keep track of the current domain.
                                        String domain;

                                        if (domains.contains("|")) {  // There is more than one domain in the list.
                                            // Get the first domain from the list.
                                            domain = domains.substring(0, domains.indexOf("|"));

                                            // Remove the first domain from the list.
                                            domains = domains.substring(domains.indexOf("|") + 1);
                                        } else {  // There is only one domain in the list.
                                            domain = domains;
                                        }

                                        if (entry.contains("*")) {  // Three are two or more entries.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            if (secondEntry.contains("*")) {  // Process a third-party domain initial black list triple entry.
                                                // Get the index of the wildcard.
                                                int secondWildcardIndex = secondEntry.indexOf("*");

                                                // Split the entry into components.
                                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                                // Create an entry string array.
                                                String[] tripleDomainEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                                // Add the entry to the black list.
                                                thirdPartyDomainInitialBlackList.add(tripleDomainEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " third-party domain initial black list added: " + domain + " , " + firstEntry + " , " + realSecondEntry +
                                                //        " , " + thirdEntry + "  -  " + originalBlockListEntry);
                                            } else {  // Process a third-party domain initial black list double entry.
                                                // Create an entry string array.
                                                String[] doubleDomainEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                                // Add the entry to the black list.
                                                thirdPartyDomainInitialBlackList.add(doubleDomainEntry);

                                                //Log.i("BlockLists", headers.get(1)[0] + " third-party domain initial black list added: " + domain + " , " + firstEntry + " , " + secondEntry +
                                                //        "  -  " + originalBlockListEntry);
                                            }
                                        } else {  // Process a third-party domain initial black list single entry.
                                            // Create an entry string array.
                                            String[] singleEntry = {domain, entry, originalBlockListEntry};

                                            // Add the entry to the black list.
                                            thirdPartyDomainInitialBlackList.add(singleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party domain initial black list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                        }
                                    } while (domains.contains("|"));
                                }
                            } else if (blockListEntry.contains("\\")) {  // Process a third-party domain black list regular expression.
                                // Parse the entry.  At least one regular expression in this entry contains `$`, so the parser uses `/$`.
                                String entry = blockListEntry.substring(0, blockListEntry.indexOf("/$") + 1);
                                String filters = blockListEntry.substring(blockListEntry.indexOf("/$") + 2);
                                String domains = filters.substring(filters.indexOf("domain=") + 7);

                                // Process each domain.
                                do {
                                    // Create a string to keep track of the current domain.
                                    String domain;

                                    if (domains.contains("|")) {  // There is more than one domain in the list.
                                        // Get the first domain from the list.
                                        domain = domains.substring(0, domains.indexOf("|"));

                                        // Remove the first domain from the list.
                                        domains = domains.substring(domains.indexOf("|") + 1);
                                    } else {  // There is only one domain in the list.
                                        domain = domains;
                                    }

                                    // Create an entry string array.
                                    String[] domainEntry = {domain, entry, originalBlockListEntry};

                                    // Add the entry to the black list.
                                    thirdPartyDomainRegularExpressionBlackList.add(domainEntry);

                                    //Log.i("BlockLists", headers.get(1)[0] + " third-party domain regular expression black list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                } while (domains.contains("|"));
                            } else {  // Third-party domain entries.
                                // Parse the entry
                                String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                                String filters = blockListEntry.substring(blockListEntry.indexOf("$") + 1);
                                String domains = filters.substring(filters.indexOf("domain=") + 7);

                                // Strip any trailing "*" from the entry.
                                if (entry.endsWith("*")) {
                                    entry = entry.substring(0, entry.length() - 1);
                                }

                                // Track if any third-party white list filters are applied.
                                boolean whiteListDomain = false;

                                // Process each domain.
                                do {
                                    // Create a string to keep track of the current domain.
                                    String domain;

                                    if (domains.contains("|")) {  // There is more than one domain in the list.
                                        // Get the first domain from the list.
                                        domain = domains.substring(0, domains.indexOf("|"));

                                        // Remove the first domain from the list.
                                        domains = domains.substring(domains.indexOf("|") + 1);
                                    } else {  // The is only one domain in the list.
                                        domain = domains;
                                    }

                                    // Differentiate between block list domains and white list domains.
                                    if (domain.startsWith("~")) {  // White list third-party domain entry.
                                        // Strip the initial `~`.
                                        domain = domain.substring(1);

                                        // Set the white list domain flag.
                                        whiteListDomain = true;

                                        if (entry.contains("*")) {  // Process a third-party domain white list double entry.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhiteList.add(domainDoubleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlockListEntry);
                                        } else {  // Process a third-party domain white list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlockListEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhiteList.add(domainEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                        }
                                    } else {  // Third-party domain black list entries.
                                        if (entry.contains("*")) {  // Process a third-party domain black list double entry.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                            // Add the entry to the black list
                                            thirdPartyDomainBlackList.add(domainDoubleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party domain black list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlockListEntry);
                                        } else {  // Process a third-party domain black list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlockListEntry};

                                            // Add the entry to the black list.
                                            thirdPartyDomainBlackList.add(domainEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " third-party domain block list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                        }
                                    }
                                } while (domains.contains("|"));

                                // Add a third-party black list entry if a white list domain was processed.
                                if (whiteListDomain) {
                                    if (entry.contains("*")) {  // Process a third-party black list double entry.
                                        // Get the index of the wildcard.
                                        int wildcardIndex = entry.indexOf("*");

                                        // Split the entry into components.
                                        String firstEntry = entry.substring(0, wildcardIndex);
                                        String secondEntry = entry.substring(wildcardIndex + 1);

                                        // Create an entry string array.
                                        String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        thirdPartyBlackList.add(doubleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                                    } else {  // Process a third-party black list single entry.
                                        // Create an entry string array.
                                        String[] singleEntry = {entry, originalBlockListEntry};

                                        // Add an entry to the black list.
                                        thirdPartyBlackList.add(singleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " third-party black list added: " + entry + "  -  " + originalBlockListEntry);
                                    }
                                }
                            }
                        } else if (blockListEntry.startsWith("|")) {  // Third-party initial black list entries.
                            // Strip the initial `|`.
                            blockListEntry = blockListEntry.substring(1);

                            // Get the entry.
                            String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));

                            if (entry.contains("*")) {  // Process a third-party initial black list double entry.
                                // Get the index of the wildcard.
                                int wildcardIndex = entry.indexOf("*");

                                // Split the entry into components.
                                String firstEntry = entry.substring(0, wildcardIndex);
                                String secondEntry = entry.substring(wildcardIndex + 1);

                                // Create an entry string array.
                                String[] thirdPartyDoubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                // Add the entry to the black list.
                                thirdPartyInitialBlackList.add(thirdPartyDoubleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " third-party initial black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                            } else {  // Process a third-party initial black list single entry.
                                // Create an entry string array.
                                String[] singleEntry = {entry, originalBlockListEntry};

                                // Add the entry to the black list.
                                thirdPartyInitialBlackList.add(singleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " third-party initial black list added: " + entry + "  -  " + originalBlockListEntry);
                            }
                        } else if (blockListEntry.contains("\\")) {  // Process a regular expression black list entry.
                            // Prepare a string to hold the entry.
                            String entry;

                            // Get the entry.
                            if (blockListEntry.contains("$/$")) {  // The first `$` is part of the regular expression.
                                entry = blockListEntry.substring(0, blockListEntry.indexOf("$/$") + 2);
                            } else {  // The only `$` indicates the filter options.
                                entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                            }

                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlockListEntry};

                            // Add the entry to the black list.
                            thirdPartyRegularExpressionBlackList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " third-party regular expression black list added: " + entry + "  -  " + originalBlockListEntry);
                        } else if (blockListEntry.contains("*")) {  // Third-party and regular expression black list entries.
                            // Get the entry.
                            String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));

                            if (entry.endsWith("*")) {  // Process a third-party black list single entry.
                                // Strip the final `*`.
                                entry = entry.substring(0, entry.length() - 1);

                                // Create an entry string array.
                                String[] singleEntry = {entry, originalBlockListEntry};

                                // Add the entry to the black list.
                                thirdPartyBlackList.add(singleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " third party black list added: " + entry + "  -  " + originalBlockListEntry);
                            } else {  // There are two or more entries.
                                // Get the index of the wildcard.
                                int wildcardIndex = entry.indexOf("*");

                                // Split the entry into components.
                                String firstEntry = entry.substring(0, wildcardIndex);
                                String secondEntry = entry.substring(wildcardIndex + 1);

                                if (secondEntry.contains("*")) {  // There are three or more entries.
                                    // Get the index of the wildcard.
                                    int secondWildcardIndex = secondEntry.indexOf("*");

                                    // Split the entry into components.
                                    String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                    String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                    if (thirdEntry.contains("*")) {  // Process a third-party black list quadruple entry.
                                        // Get the index of the wildcard.
                                        int thirdWildcardIndex = thirdEntry.indexOf("*");

                                        // Split the entry into components.
                                        String realThirdEntry = thirdEntry.substring(0, thirdWildcardIndex);
                                        String fourthEntry = thirdEntry.substring(thirdWildcardIndex + 1);

                                        // Create an entry string array.
                                        String[] quadrupleEntry = {firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        thirdPartyBlackList.add(quadrupleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                        //        fourthEntry + "  -  " + originalBlockListEntry);
                                    } else {  // Process a third-party black list triple entry.
                                        // Create an entry string array.
                                        String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        thirdPartyBlackList.add(tripleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                        //        originalBlockListEntry);
                                    }
                                } else {  // Process a third-party black list double entry.
                                    // Create an entry string array.
                                    String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                    // Add the entry to the black list.
                                    thirdPartyBlackList.add(doubleEntry);

                                    //Log.i("BlockLists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                                }
                            }
                        } else {  // Process a third party black list single entry.
                            // Get the entry.
                            String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));

                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlockListEntry};

                            // Add the entry to the black list.
                            thirdPartyBlackList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " third party black list added: " + entry + "  -  " + originalBlockListEntry);
                        }
                    } else if (blockListEntry.substring(blockListEntry.indexOf("$")).contains("domain=")) {  // Domain entries.
                        if (blockListEntry.contains("~")) {  // Domain white list entries.
                            // Separate the filters.
                            String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                            String filters = blockListEntry.substring(blockListEntry.indexOf("$") + 1);
                            String domains = filters.substring(filters.indexOf("domain=") + 7);

                            // Strip any final `*` from the entry.  They are redundant.
                            if (entry.endsWith("*")) {
                                entry = entry.substring(0, entry.length() - 1);
                            }

                            // Process each domain.
                            do {
                                // Create a string to keep track of the current domain.
                                String domain;

                                if (domains.contains("|")) {  // There is more than one domain in the list.
                                    // Get the first domain from the list.
                                    domain = domains.substring(0, domains.indexOf("|"));

                                    // Remove the first domain from the list.
                                    domains = domains.substring(domains.indexOf("|") + 1);
                                } else {  // There is only one domain in the list.
                                    domain = domains;
                                }

                                // Strip the initial `~`.
                                domain = domain.substring(1);

                                if (entry.contains("*")) {  // There are two or more entries.
                                    // Get the index of the wildcard.
                                    int wildcardIndex = entry.indexOf("*");

                                    // Split the entry into components.
                                    String firstEntry = entry.substring(0, wildcardIndex);
                                    String secondEntry = entry.substring(wildcardIndex + 1);

                                    if (secondEntry.contains("*")) {  // Process a domain white list triple entry.
                                        // Get the index of the wildcard.
                                        int secondWildcardIndex = secondEntry.indexOf("*");

                                        // Split the entry into components.
                                        String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                        String thirdEntry = secondEntry.substring((secondWildcardIndex + 1));

                                        // Create an entry string array.
                                        String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                        // Add the entry to the white list.
                                        domainWhiteList.add(domainTripleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " domain white list added: " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry +
                                        //        "  -  " + originalBlockListEntry);
                                    } else {  // Process a domain white list double entry.
                                        // Create an entry string array.
                                        String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                        // Add the entry to the white list.
                                        domainWhiteList.add(domainDoubleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " domain white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                                    }
                                } else {  // Process a domain white list single entry.
                                    // Create an entry string array.
                                    String[] domainEntry = {domain, entry, originalBlockListEntry};

                                    // Add the entry to the white list.
                                    domainWhiteList.add(domainEntry);

                                    //Log.i("BlockLists", headers.get(1)[0] + " domain white list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                }
                            } while (domains.contains("|"));
                        } else {  // Domain black list entries.
                            // Separate the filters.
                            String entry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                            String filters = blockListEntry.substring(blockListEntry.indexOf("$") + 1);
                            String domains = filters.substring(filters.indexOf("domain=") + 7);

                            // Only process the item if the entry is not null.  For example, some lines begin with `$websocket`, which create a null entry.
                            if (!entry.equals("")) {
                                // Process each domain.
                                do {
                                    // Create a string to keep track of the current domain.
                                    String domain;

                                    if (domains.contains("|")) {  // There is more than one domain in the list.
                                        // Get the first domain from the list.
                                        domain = domains.substring(0, domains.indexOf("|"));

                                        // Remove the first domain from the list.
                                        domains = domains.substring(domains.indexOf("|") + 1);
                                    } else {  // There is only one domain in the list.
                                        domain = domains;
                                    }

                                    if (entry.startsWith("|")) {  // Domain initial black list entries.
                                        // Remove the initial `|`;
                                        String entryBase = entry.substring(1);

                                        //noinspection StatementWithEmptyBody
                                        if (entryBase.equals("http://") || entryBase.equals("https://")) {
                                            // Do nothing.  These entries will entirely block the website.
                                            // Often the original entry blocks `$script` but Privacy Browser does not currently differentiate between scripts and other entries.

                                            //Log.i("BlockLists", headers.get(1)[0] + " not added: " + originalBlockListEntry);
                                        } else {  // Process a domain initial black list entry
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entryBase, originalBlockListEntry};

                                            // Add the entry to the black list.
                                            domainInitialBlackList.add(domainEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " domain initial black list added: " + domain + " , " + entryBase + "  -  " + originalBlockListEntry);
                                        }
                                    } else if (entry.endsWith("|")) {  // Domain final black list entries.
                                        // Remove the final `|`.
                                        String entryBase = entry.substring(0, entry.length() - 1);

                                        if (entryBase.contains("*")) {  // Process a domain final black list double entry.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entryBase.substring(0, wildcardIndex);
                                            String secondEntry = entryBase.substring(wildcardIndex + 1);

                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                            // Add the entry to the black list.
                                            domainFinalBlackList.add(domainDoubleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " domain final black list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlockListEntry);
                                        } else {  // Process a domain final black list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entryBase, originalBlockListEntry};

                                            // Add the entry to the black list.
                                            domainFinalBlackList.add(domainEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " domain final black list added: " + domain + " , " + entryBase + "  -  " + originalBlockListEntry);
                                        }
                                    } else if (entry.contains("\\")) {  // Process a domain regular expression black list entry.
                                        // Create an entry string array.
                                        String[] domainEntry = {domain, entry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        domainRegularExpressionBlackList.add(domainEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " domain regular expression black list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                    } else if (entry.contains("*")) {  // There are two or more entries.
                                        // Get the index of the wildcard.
                                        int wildcardIndex = entry.indexOf("*");

                                        // Split the entry into components.
                                        String firstEntry = entry.substring(0, wildcardIndex);
                                        String secondEntry = entry.substring(wildcardIndex + 1);

                                        if (secondEntry.contains("*")) {  // Process a domain black list triple entry.
                                            // Get the index of the wildcard.
                                            int secondWildcardIndex = secondEntry.indexOf("*");

                                            // Split the entry into components.
                                            String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                            String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                            // Create an entry string array.
                                            String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                            // Add the entry to the black list.
                                            domainBlackList.add(domainTripleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " domain black list added: " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry +
                                            //        "  -  " + originalBlockListEntry);
                                        } else {  // Process a domain black list double entry.
                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlockListEntry};

                                            // Add the entry to the black list.
                                            domainBlackList.add(domainDoubleEntry);

                                            //Log.i("BlockLists", headers.get(1)[0] + " domain black list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlockListEntry);
                                        }
                                    } else {  // Process a domain black list single entry.
                                        // Create an entry string array.
                                        String[] domainEntry = {domain, entry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        domainBlackList.add(domainEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " domain black list added: " + domain + " , " + entry + "  -  " + originalBlockListEntry);
                                    }
                                } while (domains.contains("|"));
                            }
                        }
                    } else if (blockListEntry.contains("~")) {  // White list entries.  Privacy Browser does not differentiate against these filter options, so they are just generally white listed.
                        // Remove the filter options.
                        blockListEntry = blockListEntry.substring(0, blockListEntry.indexOf("$"));

                        // Strip any trailing `*`.
                        if (blockListEntry.endsWith("*")) {
                            blockListEntry = blockListEntry.substring(0, blockListEntry.length() - 1);
                        }

                        if (blockListEntry.contains("*")) {  // Process a white list double entry.
                            // Get the index of the wildcard.
                            int wildcardIndex = blockListEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blockListEntry.substring(0, wildcardIndex);
                            String secondEntry = blockListEntry.substring(wildcardIndex + 1);

                            // Create an entry string array.
                            String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                            // Add the entry to the white list.
                            mainWhiteList.add(doubleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " main white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                        } else {  // Process a white list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blockListEntry, originalBlockListEntry};

                            // Add the entry to the white list.
                            mainWhiteList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " main white list added: " + blockListEntry + "  -  + " + originalBlockListEntry);
                        }
                    } else if (blockListEntry.contains("\\")) {  // Process a regular expression black list entry.
                        // Remove the filter options.
                        blockListEntry = blockListEntry.substring(0, blockListEntry.indexOf("$"));

                        // Create an entry string array.
                        String[] singleEntry = {blockListEntry, originalBlockListEntry};

                        // Add the entry to the black list.
                        regularExpressionBlackList.add(singleEntry);

                        //Log.i("BlockLists", headers.get(1)[0] + " regular expression black list added: " + blockListEntry + "  -  " + originalBlockListEntry);
                    } else {  // Black list entries.
                        // Remove the filter options.
                        if (!blockListEntry.contains("$file")) {  // EasyPrivacy contains an entry with `$file` that does not have filter options.
                            blockListEntry = blockListEntry.substring(0, blockListEntry.indexOf("$"));
                        }

                        // Strip any trailing `*`.  These are redundant.
                        if (blockListEntry.endsWith("*")) {
                            blockListEntry = blockListEntry.substring(0, blockListEntry.length() - 1);
                        }

                        if (blockListEntry.startsWith("|")) {  // Initial black list entries.
                            // Strip the initial `|`.
                            String entry = blockListEntry.substring(1);

                            if (entry.contains("*")) {  // Process an initial black list double entry.
                                // Get the index of the wildcard.
                                int wildcardIndex = entry.indexOf("*");

                                // Split the entry into components.
                                String firstEntry = entry.substring(0, wildcardIndex);
                                String secondEntry = entry.substring(wildcardIndex + 1);

                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                // Add the entry to the black list.
                                initialBlackList.add(doubleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " initial black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                            } else {  // Process an initial black list single entry.
                                // Create an entry string array.
                                String[] singleEntry = {entry, originalBlockListEntry};

                                // Add the entry to the black list.
                                initialBlackList.add(singleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " initial black list added: " + entry + "  -  " + originalBlockListEntry);
                            }
                        } else if (blockListEntry.endsWith("|")) {  // Final black list entries.
                            // Ignore entries with `object` filters.  They can block entire websites and don't have any meaning in the context of Privacy Browser.
                            if (!originalBlockListEntry.contains("$object")) {
                                // Strip the final `|`.
                                String entry = blockListEntry.substring(0, blockListEntry.length() - 1);

                                if (entry.contains("*")) {  // There are two or more entries.
                                    // Get the index of the wildcard.
                                    int wildcardIndex = entry.indexOf("*");

                                    // Split the entry into components.
                                    String firstEntry = entry.substring(0, wildcardIndex);
                                    String secondEntry = entry.substring(wildcardIndex + 1);

                                    if (secondEntry.contains("*")) {  // Process a final black list triple entry.
                                        // Get the index of the wildcard.
                                        int secondWildcardIndex = secondEntry.indexOf("*");

                                        // Split the entry into components.
                                        String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                        String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                        // Create an entry string array.
                                        String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        finalBlackList.add(tripleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                        //        originalBlockListEntry);
                                    } else {  // Process a final black list double entry.
                                        // Create an entry string array.
                                        String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        finalBlackList.add(doubleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                                    }
                                } else {  // Process a final black list single entry.
                                    // Create an entry sting array.
                                    String[] singleEntry = {entry, originalBlockListEntry};

                                    // Add the entry to the black list.
                                    finalBlackList.add(singleEntry);

                                    //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + entry + "  -  " + originalBlockListEntry);
                                }
                            }
                        } else if (blockListEntry.contains("*")) {  // There are two or more entries.
                            // Get the index of the wildcard.
                            int wildcardIndex = blockListEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blockListEntry.substring(0, wildcardIndex);
                            String secondEntry = blockListEntry.substring(wildcardIndex + 1);

                            if (secondEntry.contains("*")) {  // Process a main black list triple entry.
                                // Get the index of the wildcard.
                                int secondWildcardIndex = secondEntry.indexOf("*");

                                // Split the entry into components.
                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                // Create an entry string array.
                                String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                // Add the entry to the black list.
                                mainBlackList.add(tripleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " + originalBlockListEntry);
                            } else {  // Process a main black list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                // Add the entry to the black list.
                                mainBlackList.add(doubleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                            }
                        } else {  // Process a main black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blockListEntry, originalBlockListEntry};

                            // Add the entry to the black list.
                            mainBlackList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + blockListEntry + "  -  " + originalBlockListEntry);
                        }
                    }
                } else {  // Main black list entries
                    // Strip out any initial `||`.  These will be treated like any other entry.
                    if (blockListEntry.startsWith("||")) {
                        blockListEntry = blockListEntry.substring(2);
                    }

                    // Strip out any initial `*`.
                    if (blockListEntry.startsWith("*")) {
                        blockListEntry = blockListEntry.substring(1);
                    }

                    // Strip out any trailing `*`.
                    if (blockListEntry.endsWith("*")) {
                        blockListEntry = blockListEntry.substring(0, blockListEntry.length() - 1);
                    }

                    if (blockListEntry.startsWith("|")) {  // Initial black list entries.
                        // Strip the initial `|`.
                        String entry = blockListEntry.substring(1);

                        if (entry.contains("*")) {  // Process an initial black list double entry.
                            // Get the index of the wildcard.
                            int wildcardIndex = entry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = entry.substring(0, wildcardIndex);
                            String secondEntry = entry.substring(wildcardIndex + 1);

                            // Create an entry string array.
                            String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                            // Add the entry to the black list.
                            initialBlackList.add(doubleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " initial black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                        } else {  // Process an initial black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlockListEntry};

                            // Add the entry to the black list.
                            initialBlackList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " initial black list added: " + entry + "  -  " + originalBlockListEntry);
                        }
                    } else if (blockListEntry.endsWith("|")) {  // Final black list entries.
                        // Strip the final `|`.
                        String entry = blockListEntry.substring(0, blockListEntry.length() - 1);

                        if (entry.contains("*")) {  // There are two or more entries.
                            // Get the index of the wildcard.
                            int wildcardIndex = entry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = entry.substring(0, wildcardIndex);
                            String secondEntry = entry.substring(wildcardIndex + 1);

                            if (secondEntry.contains("*")) {  // Process a final black list triple entry.
                                // Get the index of the wildcard.
                                int secondWildcardIndex = secondEntry.indexOf("*");

                                // Split the entry into components.
                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                // Create an entry string array.
                                String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                // Add the entry to the black list.
                                finalBlackList.add(tripleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                //        originalBlockListEntry);
                            } else {  // Process a final black list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                // Add the entry to the black list.
                                finalBlackList.add(doubleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                            }
                        } else {  // Process a final black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlockListEntry};

                            // Add the entry to the black list.
                            finalBlackList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " final black list added: " + entry + "  -  " + originalBlockListEntry);
                        }
                    } else {  // Main black list entries.
                        if (blockListEntry.contains("*")) {  // There are two or more entries.
                            // Get the index of the wildcard.
                            int wildcardIndex = blockListEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blockListEntry.substring(0, wildcardIndex);
                            String secondEntry = blockListEntry.substring(wildcardIndex + 1);

                            if (secondEntry.contains("*")) {  // There are three or more entries.
                                // Get the index of the wildcard.
                                int secondWildcardIndex = secondEntry.indexOf("*");

                                // Split the entry into components.
                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                if (thirdEntry.contains("*")) {  // There are four or more entries.
                                    // Get the index of the wildcard.
                                    int thirdWildcardIndex = thirdEntry.indexOf("*");

                                    // Split the entry into components.
                                    String realThirdEntry = thirdEntry.substring(0, thirdWildcardIndex);
                                    String fourthEntry = thirdEntry.substring(thirdWildcardIndex + 1);

                                    if (fourthEntry.contains("*")) {  // Process a main black list quintuple entry.
                                        // Get the index of the wildcard.
                                        int fourthWildcardIndex = fourthEntry.indexOf("*");

                                        // Split the entry into components.
                                        String realFourthEntry = fourthEntry.substring(0, fourthWildcardIndex);
                                        String fifthEntry = fourthEntry.substring(fourthWildcardIndex + 1);

                                        // Create an entry string array.
                                        String[] quintupleEntry = {firstEntry, realSecondEntry, realThirdEntry, realFourthEntry, fifthEntry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        mainBlackList.add(quintupleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                        //        realFourthEntry + " , " + fifthEntry + "  -  " + originalBlockListEntry);
                                    } else {  // Process a main black list quadruple entry.
                                        // Create an entry string array.
                                        String[] quadrupleEntry = {firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlockListEntry};

                                        // Add the entry to the black list.
                                        mainBlackList.add(quadrupleEntry);

                                        //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                        //        fourthEntry + "  -  " + originalBlockListEntry);
                                    }
                                } else {  // Process a main black list triple entry.
                                    // Create an entry string array.
                                    String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlockListEntry};

                                    // Add the entry to the black list.
                                    mainBlackList.add(tripleEntry);

                                    //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " + originalBlockListEntry);
                                }
                            } else {  // Process a main black list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlockListEntry};

                                // Add the entry to the black list.
                                mainBlackList.add(doubleEntry);

                                //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlockListEntry);
                            }
                        } else {  // Process a main black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blockListEntry, originalBlockListEntry};

                            // Add the entry to the black list.
                            mainBlackList.add(singleEntry);

                            //Log.i("BlockLists", headers.get(1)[0] + " main black list added: " + blockListEntry + "  -  " + originalBlockListEntry);
                        }
                    }
                }
            }
            // Close `bufferedReader`.
            bufferedReader.close();
        } catch (IOException e) {
            // The asset exists, so the `IOException` will never be thrown.
        }

        // Initialize the combined list.
        ArrayList<List<String[]>> combinedLists = new ArrayList<>();

        // Add the headers (0).
        combinedLists.add(headers);  // 0.

        // Add the white lists (1-8).
        combinedLists.add(mainWhiteList);  // 1.
        combinedLists.add(finalWhiteList);  // 2.
        combinedLists.add(domainWhiteList);  // 3.
        combinedLists.add(domainInitialWhiteList);  // 4.
        combinedLists.add(domainFinalWhiteList); // 5.
        combinedLists.add(thirdPartyWhiteList);  // 6.
        combinedLists.add(thirdPartyDomainWhiteList);  // 7.
        combinedLists.add(thirdPartyDomainInitialWhiteList);  // 8.

        // Add the black lists (9-22).
        combinedLists.add(mainBlackList);  // 9.
        combinedLists.add(initialBlackList);  // 10.
        combinedLists.add(finalBlackList);  // 11.
        combinedLists.add(domainBlackList);  //  12.
        combinedLists.add(domainInitialBlackList);  // 13.
        combinedLists.add(domainFinalBlackList);  // 14.
        combinedLists.add(domainRegularExpressionBlackList);  // 15.
        combinedLists.add(thirdPartyBlackList);  // 16.
        combinedLists.add(thirdPartyInitialBlackList);  // 17.
        combinedLists.add(thirdPartyDomainBlackList);  // 18.
        combinedLists.add(thirdPartyDomainInitialBlackList);  // 19.
        combinedLists.add(thirdPartyRegularExpressionBlackList);  // 20.
        combinedLists.add(thirdPartyDomainRegularExpressionBlackList);  // 21.
        combinedLists.add(regularExpressionBlackList);  // 22.

        return combinedLists;
    }

    public boolean isBlocked(String currentDomain, String resourceUrl, boolean isThirdPartyRequest, ArrayList<List<String[]>> blockList) {
        // Get the block list name.
        String BLOCK_LIST_NAME_STRING = blockList.get(0).get(1)[0];

        // Assert that currentDomain != null only if this is a third party request.  Apparently, lint can't tell that this isn't redundant.
        //noinspection RedundantIfStatement
        if (isThirdPartyRequest) {
            assert currentDomain != null;
        }

        // Process the white lists.
        // Main white list.
        for (String[] whiteListEntry : blockList.get(MainWebViewActivity.MAIN_WHITELIST)) {
            switch (whiteListEntry.length) {
                case 2:  // There is one entry.
                    if (resourceUrl.contains(whiteListEntry[0])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_WHITELIST), whiteListEntry[0], whiteListEntry[1]};

                        // Not blocked.
                        return false;
                    }
                    break;

                case 3:  // There are two entries.
                    if (resourceUrl.contains(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1],
                                whiteListEntry[2]};

                        // Not blocked.
                        return false;
                    }
                    break;

                case 4:  // There are three entries.
                    if (resourceUrl.contains(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2], whiteListEntry[3]};

                        // Not blocked.
                        return false;
                    }
                    break;
            }
        }

        // Final white list.
        for (String[] whiteListEntry : blockList.get(MainWebViewActivity.FINAL_WHITELIST)) {
            if (whiteListEntry.length == 2) {  // There is one entry.
                if (resourceUrl.contains(whiteListEntry[0])) {
                    // Store the entry in the resource request log.
                    MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.FINAL_WHITELIST), whiteListEntry[0], whiteListEntry[1]};

                    // Not blocked.
                    return false;
                }
            } else {  // There are two entries.
                if (resourceUrl.contains(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1])) {
                    // Store the entry in the resource request log.
                    MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.FINAL_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1], whiteListEntry[2]};

                    // Not blocked.
                    return false;
                }
            }
        }

        // Only check the domain lists if the current domain is not null (like `about:blank`).
        if (currentDomain != null) {
            // Domain white list.
            for (String[] whiteListEntry : blockList.get(MainWebViewActivity.DOMAIN_WHITELIST)) {
                switch (whiteListEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1], whiteListEntry[2]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2], whiteListEntry[3]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2]) && resourceUrl.contains(whiteListEntry[3])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2] + "\n" + whiteListEntry[3],
                                    whiteListEntry[4]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 6:  // There are four entries.
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2]) && resourceUrl.contains(whiteListEntry[3]) &&
                                resourceUrl.contains(whiteListEntry[4])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2] + "\n" + whiteListEntry[3] + "\n" +
                                    whiteListEntry[4], whiteListEntry[5]};

                            // Not blocked.
                            return false;
                        }
                        break;
                }
            }

            // Domain initial white list.
            for (String[] whiteListEntry : blockList.get(MainWebViewActivity.DOMAIN_INITIAL_WHITELIST)) {
                switch (whiteListEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.startsWith(whiteListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_INITIAL_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1], whiteListEntry[2]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.startsWith(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_INITIAL_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2], whiteListEntry[3]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.startsWith(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2]) && resourceUrl.startsWith(whiteListEntry[3])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_INITIAL_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2] + "\n" + whiteListEntry[3],
                                    whiteListEntry[4]};

                            // Not blocked.
                            return false;
                        }
                        break;
                }
            }

            // Domain final white list.
            for (String[] whiteListEntry : blockList.get(MainWebViewActivity.DOMAIN_FINAL_WHITELIST)) {
                switch (whiteListEntry.length) {
                    case 3:  // There is one entry;
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.endsWith(whiteListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_FINAL_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1], whiteListEntry[2]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 4:  // There are two entries;
                        if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.endsWith(whiteListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_FINAL_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2], whiteListEntry[3]};


                            // Not blocked.
                            return false;
                        }
                        break;
                }
            }
        }

        // Only check the third-party white lists if this is a third-party request.
        if (isThirdPartyRequest) {
            // Third-party white list.
            for (String[] whiteListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_WHITELIST)) {
                switch (whiteListEntry.length) {
                    case 2:  // There is one entry
                        if (resourceUrl.contains(whiteListEntry[0])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_WHITELIST), whiteListEntry[0], whiteListEntry[1]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 3:  // There are two entries.
                        if (resourceUrl.contains(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1], whiteListEntry[2]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 4:  // There are three entries.
                        if (resourceUrl.contains(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2], whiteListEntry[3]};

                            // Not blocked.
                            return false;
                        }
                        break;

                    case 5:  // There are four entries.
                        if (resourceUrl.contains(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2]) && resourceUrl.contains(whiteListEntry[3])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2] + "\n" + whiteListEntry[3],
                                    whiteListEntry[4]};
                            // Not blocked.
                            return false;
                        }
                        break;

                    case 6:  // There are five entries.
                        if (resourceUrl.contains(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2]) && resourceUrl.contains(whiteListEntry[3]) &&
                                resourceUrl.contains(whiteListEntry[4])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2] + "\n" + whiteListEntry[3] + "\n" +
                                    whiteListEntry[4], whiteListEntry[5]};

                            // Not blocked.
                            return false;
                        }
                        break;
                }
            }

            // Third-party domain white list.
            for (String[] whiteListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_DOMAIN_WHITELIST)) {
                if (whiteListEntry.length == 3) {  // There is one entry.
                    if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1], whiteListEntry[2]};

                        // Not blocked.
                        return false;
                    }
                } else {  // There are two entries.
                    if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.contains(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2], whiteListEntry[3]};

                        // Not blocked.
                        return false;
                    }
                }
            }

            // Third-party domain initial white list.
            for (String[] whiteListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_DOMAIN_INITIAL_WHITELIST)) {
                if (whiteListEntry.length == 3) {  // There is one entry.
                    if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.startsWith(whiteListEntry[1])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_INITIAL_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1], whiteListEntry[2]};

                        // Not blocked.
                        return false;
                    }
                } else {  // There are two entries.
                    if (currentDomain.endsWith(whiteListEntry[0]) && resourceUrl.startsWith(whiteListEntry[1]) && resourceUrl.contains(whiteListEntry[2])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.whiteListResultStringArray = new String[] {String.valueOf(MainWebViewActivity.REQUEST_ALLOWED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_WHITELIST), whiteListEntry[0] + "\n" + whiteListEntry[1] + "\n" + whiteListEntry[2], whiteListEntry[3]};

                        // Not blocked.
                        return false;
                    }
                }
            }
        }

        // Process the black lists.
        // Main black list.
        for (String[] blackListEntry : blockList.get(MainWebViewActivity.MAIN_BLACKLIST)) {
            switch (blackListEntry.length) {
                case 2:  // There is one entry.
                    if (resourceUrl.contains(blackListEntry[0])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_BLACKLIST), blackListEntry[0], blackListEntry[1]});

                        // Blocked.
                        return true;
                    }
                    break;

                case 3:  // There are two entries.
                    if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                        // Blocked.
                        return true;
                    }
                    break;

                case 4:  // There are three entries.
                    if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2], blackListEntry[3]});

                        // Blocked.
                        return true;
                    }
                    break;

                case 5:  // There are four entries.
                    if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2]) && resourceUrl.contains(blackListEntry[3])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2] + "\n" + blackListEntry[3], blackListEntry[4]});

                        // Blocked.
                        return true;
                    }
                    break;

                case 6:  // There are five entries.
                    if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2]) && resourceUrl.contains(blackListEntry[3]) &&
                            resourceUrl.contains(blackListEntry[4])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.MAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2] + "\n" + blackListEntry[3] + "\n" +
                                blackListEntry[4], blackListEntry[5]});

                        // Blocked.
                        return true;
                    }
                    break;
            }
        }

        // Initial black list.
        for (String[] blackListEntry : blockList.get(MainWebViewActivity.INITIAL_BLACKLIST)) {
            if (blackListEntry.length == 2) {  // There is one entry.
                if (resourceUrl.startsWith(blackListEntry[0])) {
                    // Store the entry in the resource request log.
                    MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.INITIAL_BLACKLIST), blackListEntry[0], blackListEntry[1]});

                    // Blocked.
                    return true;
                }
            } else {  // There are two entries
                if (resourceUrl.startsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1])) {
                    // Store the entry in the resource request log.
                    MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.INITIAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                    // Blocked.
                    return true;
                }
            }
        }

        // Final black list.
        for (String[] blackListEntry : blockList.get(MainWebViewActivity.FINAL_BLACKLIST)) {
            switch (blackListEntry.length) {
                case 2:  // There is one entry.
                    if (resourceUrl.endsWith(blackListEntry[0])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.FINAL_BLACKLIST), blackListEntry[0], blackListEntry[1]});

                        // Blocked.
                        return true;
                    }
                    break;

                case 3:  // There are two entries.
                    if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.endsWith(blackListEntry[1])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.FINAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                        // Blocked.
                        return true;
                    }
                    break;

                case 4:  // There are three entries.
                    if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.endsWith(blackListEntry[2])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.FINAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2], blackListEntry[3]});

                        // Blocked.
                        return true;
                    }
                    break;
            }
        }

        // Only check the domain lists if the current domain is not null (like `about:blank`).
        if (currentDomain != null) {
            // Domain black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.DOMAIN_BLACKLIST)) {
                switch (blackListEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                            // Blocked.
                            return true;
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2], blackListEntry[3]});

                            // Blocked.
                            return true;
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2]) && resourceUrl.contains(blackListEntry[3])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2] + "\n" + blackListEntry[3],
                                    blackListEntry[4]});

                            // Blocked.
                            return true;
                        }
                        break;
                }
            }

            // Domain initial black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.DOMAIN_INITIAL_BLACKLIST)) {
                // Store the entry in the resource request log.
                if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.startsWith(blackListEntry[1])) {
                    MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.DOMAIN_INITIAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1],
                            blackListEntry[2]});

                    // Blocked.
                    return true;
                }
            }

            // Domain final black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.DOMAIN_FINAL_BLACKLIST)) {
                switch (blackListEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.endsWith(blackListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_FINAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                            // Blocked.
                            return true;
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.endsWith(blackListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.DOMAIN_FINAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2], blackListEntry[3]});

                            // Blocked.
                            return true;
                        }
                        break;
                }
            }

            // Domain regular expression black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.DOMAIN_REGULAR_EXPRESSION_BLACKLIST)) {
                if (currentDomain.endsWith(blackListEntry[0]) && Pattern.matches(blackListEntry[1], resourceUrl)) {
                    // Store the entry in the resource request log.
                    MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.DOMAIN_REGULAR_EXPRESSION_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                    // Blocked.
                    return true;
                }
            }
        }

        // Only check the third-party black lists if this is a third-party request.
        if (isThirdPartyRequest) {
            // Third-party black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_BLACKLIST)) {
                switch (blackListEntry.length) {
                    case 2:  // There is one entry.
                        if (resourceUrl.contains(blackListEntry[0])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_BLACKLIST), blackListEntry[0], blackListEntry[1]});

                            // Blocked.
                            return true;
                        }
                        break;

                    case 3:  // There are two entries.
                        if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                            // Blocked.
                            return true;
                        }
                        break;

                    case 4:  // There are three entries.
                        if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2], blackListEntry[3]});

                            // Blocked.
                            return true;
                        }
                        break;

                    case 5:  // There are four entries.
                        if (resourceUrl.contains(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2]) && resourceUrl.contains(blackListEntry[3])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2] + "\n" + blackListEntry[3],
                                    blackListEntry[4]});

                            // Blocked.
                            return true;
                        }
                        break;
                }
            }

            // Third-party initial black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_INITIAL_BLACKLIST)) {
                if (blackListEntry.length == 2) {  // There is one entry.
                    if (resourceUrl.startsWith(blackListEntry[0])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_INITIAL_BLACKLIST), blackListEntry[0], blackListEntry[1]});

                        // Blocked.
                        return true;
                    }
                } else {  // There are two entries.
                    if (resourceUrl.startsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_INITIAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                        // Blocked.
                        return true;
                    }
                }
            }

            // Third-party domain black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_DOMAIN_BLACKLIST)) {
                if (blackListEntry.length == 3) {  // There is one entry.
                    if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                        // Blocked.
                        return true;
                    }
                } else { // There are two entries.
                    if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.contains(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2])) {
                        // Store the entry in the resource request log.
                        MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2], blackListEntry[3]});

                        // Blocked.
                        return true;
                    }
                }
            }

            // Third-party domain initial black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST)) {
                switch (blackListEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.startsWith(blackListEntry[1])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});
                            // Blocked.
                            return true;
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.startsWith(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2], blackListEntry[3]});

                            // Blocked.
                            return true;
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(blackListEntry[0]) && resourceUrl.startsWith(blackListEntry[1]) && resourceUrl.contains(blackListEntry[2]) && resourceUrl.contains(blackListEntry[3])) {
                            // Store the entry in the resource request log.
                            MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                                    String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1] + "\n" + blackListEntry[2] + "\n" + blackListEntry[3],
                                    blackListEntry[4]});

                            // Blocked.
                            return true;
                        }
                        break;
                }
            }

            // Third-party regular expression black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_REGULAR_EXPRESSION_BLACKLIST)) {
                if (Pattern.matches(blackListEntry[0], resourceUrl)) {
                    // Store the entry in the resource request log.
                    MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.THIRD_PARTY_REGULAR_EXPRESSION_BLACKLIST), blackListEntry[0], blackListEntry[1]});

                    // Blocked.
                    return true;
                }
            }

            // Third-party domain regular expression black list.
            for (String[] blackListEntry : blockList.get(MainWebViewActivity.THIRD_PARTY_DOMAIN_REGULAR_EXPRESSION_BLACKLIST)) {
                if (currentDomain.endsWith(blackListEntry[0]) && Pattern.matches(blackListEntry[1], resourceUrl)) {
                    // Store the entry in the resource request log.
                    MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                            String.valueOf(MainWebViewActivity.THIRD_PARTY_DOMAIN_REGULAR_EXPRESSION_BLACKLIST), blackListEntry[0] + "\n" + blackListEntry[1], blackListEntry[2]});

                    // Blocked.
                    return true;
                }
            }
        }

        // Regular expression black list.
        for (String[] blackListEntry : blockList.get(MainWebViewActivity.REGULAR_EXPRESSION_BLACKLIST)) {
            if (Pattern.matches(blackListEntry[0], resourceUrl)) {
                // Store the entry in the resource request log.
                MainWebViewActivity.resourceRequests.add(new String[] {String.valueOf(MainWebViewActivity.REQUEST_BLOCKED), resourceUrl, BLOCK_LIST_NAME_STRING,
                        String.valueOf(MainWebViewActivity.REGULAR_EXPRESSION_BLACKLIST), blackListEntry[0], blackListEntry[1]});

                // blocked.
                return true;
            }
        }

        // Not blocked.
        return false;
    }
}