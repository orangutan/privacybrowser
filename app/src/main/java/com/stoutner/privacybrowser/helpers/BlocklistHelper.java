/*
 * Copyright © 2018-2019 Soren Stoutner <soren@stoutner.com>.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BlocklistHelper {
    // Describe the schema of the string array in each entry of the resource requests array list.
    public final static int REQUEST_DISPOSITION = 0;
    public final static int REQUEST_URL = 1;
    public final static int REQUEST_BLOCKLIST = 2;
    public final static int REQUEST_SUBLIST = 3;
    public final static int REQUEST_BLOCKLIST_ENTRIES = 4;
    public final static int REQUEST_BLOCKLIST_ORIGINAL_ENTRY = 5;

    // The request disposition options.
    public final static String REQUEST_DEFAULT = "0";
    public final static String REQUEST_ALLOWED = "1";
    public final static String REQUEST_THIRD_PARTY = "2";
    public final static String REQUEST_BLOCKED = "3";

    // The whitelists.
    public final static String MAIN_WHITELIST = "1";
    public final static String FINAL_WHITELIST = "2";
    public final static String DOMAIN_WHITELIST = "3";
    public final static String DOMAIN_INITIAL_WHITELIST = "4";
    public final static String DOMAIN_FINAL_WHITELIST = "5";
    public final static String THIRD_PARTY_WHITELIST = "6";
    public final static String THIRD_PARTY_DOMAIN_WHITELIST = "7";
    public final static String THIRD_PARTY_DOMAIN_INITIAL_WHITELIST = "8";

    // The blacklists.
    public final static String MAIN_BLACKLIST = "9";
    public final static String INITIAL_BLACKLIST = "10";
    public final static String FINAL_BLACKLIST = "11";
    public final static String DOMAIN_BLACKLIST = "12";
    public final static String DOMAIN_INITIAL_BLACKLIST = "13";
    public final static String DOMAIN_FINAL_BLACKLIST = "14";
    public final static String DOMAIN_REGULAR_EXPRESSION_BLACKLIST = "15";
    public final static String THIRD_PARTY_BLACKLIST = "16";
    public final static String THIRD_PARTY_INITIAL_BLACKLIST = "17";
    public final static String THIRD_PARTY_DOMAIN_BLACKLIST = "18";
    public final static String THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST = "19";
    public final static String THIRD_PARTY_REGULAR_EXPRESSION_BLACKLIST = "20";
    public final static String THIRD_PARTY_DOMAIN_REGULAR_EXPRESSION_BLACKLIST = "21";
    public final static String REGULAR_EXPRESSION_BLACKLIST = "22";

    public ArrayList<List<String[]>> parseBlocklist(AssetManager assets, String blocklistName) {
        // Initialize the header list.
        List<String[]> headers = new ArrayList<>();  // 0.

        // Initialize the whitelists.
        List<String[]> mainWhitelist = new ArrayList<>();  // 1.
        List<String[]> finalWhitelist = new ArrayList<>();  // 2.
        List<String[]> domainWhitelist = new ArrayList<>();  // 3.
        List<String[]> domainInitialWhitelist = new ArrayList<>();  // 4.
        List<String[]> domainFinalWhitelist = new ArrayList<>();  // 5.
        List<String[]> thirdPartyWhitelist = new ArrayList<>();  // 6.
        List<String[]> thirdPartyDomainWhitelist = new ArrayList<>();  // 7.
        List<String[]> thirdPartyDomainInitialWhitelist = new ArrayList<>();  // 8.

        // Initialize the blacklists
        List<String[]> mainBlacklist = new ArrayList<>();  // 9.
        List<String[]> initialBlacklist = new ArrayList<>();  // 10.
        List<String[]> finalBlacklist = new ArrayList<>();  // 11.
        List<String[]> domainBlacklist = new ArrayList<>();  // 12.
        List<String[]> domainInitialBlacklist = new ArrayList<>();  // 13.
        List<String[]> domainFinalBlacklist = new ArrayList<>();  // 14.
        List<String[]> domainRegularExpressionBlacklist = new ArrayList<>();  // 15.
        List<String[]> thirdPartyBlacklist = new ArrayList<>();  // 16.
        List<String[]> thirdPartyInitialBlacklist = new ArrayList<>();  // 17.
        List<String[]> thirdPartyDomainBlacklist = new ArrayList<>();  // 18.
        List<String[]> thirdPartyDomainInitialBlacklist = new ArrayList<>();  // 19.
        List<String[]> regularExpressionBlacklist = new ArrayList<>();  // 20.
        List<String[]> thirdPartyRegularExpressionBlacklist = new ArrayList<>();  // 21.
        List<String[]> thirdPartyDomainRegularExpressionBlacklist = new ArrayList<>();  // 22.


        // Populate the block lists.  The `try` is required by `InputStreamReader`.
        try {
            // Load the block list into a `BufferedReader`.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assets.open(blocklistName)));

            // Create a string for storing the block list entries.
            String blocklistEntry;

            // Parse the block list.
            while ((blocklistEntry = bufferedReader.readLine()) != null) {
                // Store the original block list entry.
                String originalBlocklistEntry = blocklistEntry;

                // Remove any `^` from the block list entry.  Privacy Browser does not process them in the interest of efficiency.
                blocklistEntry = blocklistEntry.replace("^", "");

                //noinspection StatementWithEmptyBody
                if (blocklistEntry.contains("##") || blocklistEntry.contains("#?#") || blocklistEntry.contains("#@#") || blocklistEntry.startsWith("[")) {
                    // Entries that contain `##`, `#?#`, and `#@#` are for hiding elements in the main page's HTML.  Entries that start with `[` describe the AdBlock compatibility level.
                    // Do nothing.  Privacy Browser does not currently use these entries.

                    //Log.i("Blocklists", "Not added: " + blocklistEntry);
                } else //noinspection StatementWithEmptyBody
                    if (blocklistEntry.contains("$csp=script-src")) {  // Ignore entries that contain `$csp=script-src`.
                        // Do nothing.  It is uncertain what this directive is even supposed to mean, and it is blocking entire websites like androidcentral.com.  https://redmine.stoutner.com/issues/306.

                        //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
                } else //noinspection StatementWithEmptyBody
                    if (blocklistEntry.contains("$websocket") || blocklistEntry.contains("$third-party,websocket") || blocklistEntry.contains("$script,websocket")) {  // Ignore entries with `websocket`.
                        // Do nothing.  Privacy Browser does not differentiate between websocket requests and other requests and these entries cause a lot of false positives.

                        //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
                } else if (blocklistEntry.startsWith("!")) {  //  Comment entries.
                    if (blocklistEntry.startsWith("! Version:")) {
                        // Get the list version number.
                        String[] listVersion = {blocklistEntry.substring(11)};

                        // Store the list version in the headers list.
                        headers.add(listVersion);
                    }

                    if (blocklistEntry.startsWith("! Title:")) {
                        // Get the list title.
                        String[] listTitle = {blocklistEntry.substring(9)};

                        // Store the list title in the headers list.
                        headers.add(listTitle);
                    }

                    //Log.i("Blocklists", "Not added: " + blocklistEntry);
                } else if (blocklistEntry.startsWith("@@")) {  // Entries that begin with `@@` are whitelists.
                    // Remove the `@@`
                    blocklistEntry = blocklistEntry.substring(2);

                    // Strip out any initial `||`.  Privacy Browser doesn't differentiate items that only match against the end of the domain name.
                    if (blocklistEntry.startsWith("||")) {
                        blocklistEntry = blocklistEntry.substring(2);
                    }

                    if (blocklistEntry.contains("$")) {  // Filter entries.
                        //noinspection StatementWithEmptyBody
                        if (blocklistEntry.contains("~third-party")) {  // Ignore entries that contain `~third-party`.
                            // Do nothing.

                            //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
                        } else if (blocklistEntry.contains("third-party")) {  // Third-party white list entries.
                            if (blocklistEntry.contains("domain=")) {  // Third-party domain white list entries.
                                // Parse the entry.
                                String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                                String filters = blocklistEntry.substring(blocklistEntry.indexOf("$") + 1);
                                String domains = filters.substring(filters.indexOf("domain=") + 7);

                                //noinspection StatementWithEmptyBody
                                if (domains.contains("~")) {  // It is uncertain what a `~` domain means inside an `@@` entry.
                                    // Do Nothing

                                    //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
                                } else if (blocklistEntry.startsWith("|")) {  // Third-party domain initial white list entries.
                                    // Strip out the initial `|`.
                                    entry = entry.substring(1);

                                    //noinspection StatementWithEmptyBody
                                    if (entry.equals("http://") || entry.equals("https://")) {  // Ignore generic entries.
                                        // Do nothing.  These entries are designed for filter options that Privacy Browser does not use.

                                        //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
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
                                                String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                                // Add the entry to the white list.
                                                thirdPartyDomainInitialWhitelist.add(domainDoubleEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " third-party domain initial white list added: " + domain + " , " + firstEntry + " , " + secondEntry +
                                                //        "  -  " + originalBlocklistEntry);
                                            } else {  // Process a third-party domain initial white list single entry.
                                                // Create a domain entry string array.
                                                String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                                // Add the entry to the third party domain initial white list.
                                                thirdPartyDomainInitialWhitelist.add(domainEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " third-party domain initial white list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
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
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhitelist.add(domainDoubleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlocklistEntry);
                                        } else {  // Process a third-party domain single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhitelist.add(domainEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                        }
                                    } while (domains.contains("|"));
                                }
                            } else {  // Process third-party white list entries.
                                // Parse the entry
                                String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));

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
                                                String[] quintupleEntry = {firstEntry, realSecondEntry, realThirdEntry, realFourthEntry, fifthEntry, originalBlocklistEntry};

                                                // Add the entry to the white list.
                                                thirdPartyWhitelist.add(quintupleEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                                //        realFourthEntry + " , " + fifthEntry + "  -  " + originalBlocklistEntry);
                                            } else {  // Process a third-party white list quadruple entry.
                                                // Create an entry string array.
                                                String[] quadrupleEntry = {firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlocklistEntry};

                                                // Add the entry to the white list.
                                                thirdPartyWhitelist.add(quadrupleEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                                //        fourthEntry + "  -  " + originalBlocklistEntry);
                                            }
                                        } else {  // Process a third-party white list triple entry.
                                            // Create an entry string array.
                                            String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                            // Add the entry to the white list.
                                            thirdPartyWhitelist.add(tripleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                            //        originalBlocklistEntry);
                                        }
                                    } else {  // Process a third-party white list double entry.
                                        // Create an entry string array.
                                        String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                        // Add the entry to the white list.
                                        thirdPartyWhitelist.add(doubleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " third-party white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                                    }
                                } else {  // Process a third-party white list single entry.
                                    // Create an entry string array.
                                    String[] singleEntry = {entry, originalBlocklistEntry};

                                    // Add the entry to the white list.
                                    thirdPartyWhitelist.add(singleEntry);

                                    //Log.i("Blocklists", headers.get(1)[0] + " third-party domain white list added: " + entry + "  -  " + originalBlocklistEntry);
                                }
                            }
                        } else if (blocklistEntry.contains("domain=")) {  // Process domain white list entries.
                            // Parse the entry
                            String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                            String filters = blocklistEntry.substring(blocklistEntry.indexOf("$") + 1);
                            String domains = filters.substring(filters.indexOf("domain=") + 7);

                            if (entry.startsWith("|")) {  // Initial domain white list entries.
                                // Strip the initial `|`.
                                entry = entry.substring(1);

                                //noinspection StatementWithEmptyBody
                                if (entry.equals("http://") || entry.equals("https://")) {  // Ignore generic entries.
                                    // Do nothing.  These entries are designed for filter options that Privacy Browser does not use.

                                    //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
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
                                                String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                                // Add the entry to the white list.
                                                domainInitialWhitelist.add(domainTripleEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " domain initial white list entry added: " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " +
                                                //        thirdEntry + "  -  " + originalBlocklistEntry);
                                            } else {  // Process a domain initial double entry.
                                                // Create an entry string array.
                                                String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                                // Add the entry to the white list.
                                                domainInitialWhitelist.add(domainDoubleEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " domain initial white list entry added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                                //        originalBlocklistEntry);
                                            }
                                        } else {  // Process a domain initial single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                            // Add the entry to the white list.
                                            domainInitialWhitelist.add(domainEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " domain initial white list entry added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
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
                                        String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                        // Add the entry to the white list.
                                        domainFinalWhitelist.add(domainDoubleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " domain final white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                        //        originalBlocklistEntry);
                                    } else {  // Process a domain final white list single entry.
                                        // create an entry string array.
                                        String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                        // Add the entry to the white list.
                                        domainFinalWhitelist.add(domainEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " domain final white list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                    }
                                } while (domains.contains("|"));

                            } else {  // Standard domain white list entries with filters.
                                //noinspection StatementWithEmptyBody
                                if (domains.contains("~")) {  // It is uncertain what a `~` domain means inside an `@@` entry.
                                    // Do Nothing

                                    //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
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
                                                    String[] domainQuadrupleEntry = {domain, firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlocklistEntry};

                                                    // Add the entry to the white list.
                                                    domainWhitelist.add(domainQuadrupleEntry);

                                                    //Log.i("Blocklists", headers.get(1)[0] + " domain white list added : " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " +
                                                    //        realThirdEntry + " , " + fourthEntry + "  -  " + originalBlocklistEntry);
                                                } else {  // Process a domain white list triple entry.
                                                    // Create an entry string array.
                                                    String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                                    // Add the entry to the white list.
                                                    domainWhitelist.add(domainTripleEntry);

                                                    //Log.i("Blocklists", headers.get(1)[0] + " domain white list added : " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " +
                                                    //        thirdEntry + "  -  " + originalBlocklistEntry);
                                                }
                                            } else {  // Process a domain white list double entry.
                                                // Create an entry string array.
                                                String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                                // Add the entry to the white list.
                                                domainWhitelist.add(domainDoubleEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " domain white list added : " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                                //        originalBlocklistEntry);
                                            }
                                        } else {  // Process a domain white list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                            // Add the entry to the white list.
                                            domainWhitelist.add(domainEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " domain white list added : " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                        }
                                    } while (domains.contains("|"));
                                }
                            }
                        }  // Ignore all other filter entries.
                    } else if (blocklistEntry.endsWith("|")) {  // Final white list entries.
                        // Remove the final `|` from the entry.
                        String entry = blocklistEntry.substring(0, blocklistEntry.length() - 1);

                        if (entry.contains("*")) {  // Process a final white list double entry
                            // Get the index of the wildcard.
                            int wildcardIndex = entry.indexOf("*");

                            // split the entry into components.
                            String firstEntry = entry.substring(0, wildcardIndex);
                            String secondEntry = entry.substring(wildcardIndex + 1);

                            // Create an entry string array.
                            String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                            // Add the entry to the white list.
                            finalWhitelist.add(doubleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " final white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                        } else {  // Process a final white list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlocklistEntry};

                            // Add the entry to the white list.
                            finalWhitelist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " final white list added: " + entry + "  -  " + originalBlocklistEntry);
                        }
                    } else {  // Main white list entries.
                        if (blocklistEntry.contains("*")) {  // There are two or more entries.
                            // Get the index of the wildcard.
                            int wildcardIndex = blocklistEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blocklistEntry.substring(0, wildcardIndex);
                            String secondEntry = blocklistEntry.substring(wildcardIndex + 1);

                            if (secondEntry.contains("*")) {  // Process a main white list triple entry.
                                // Get the index of the wildcard.
                                int secondWildcardIndex = secondEntry.indexOf("*");

                                // Split the entry into components.
                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                // Create an entry string array.
                                String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                // Add the entry to the white list.
                                mainWhitelist.add(tripleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " main white list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " + originalBlocklistEntry);
                            } else {  // Process a main white list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                // Add the entry to the white list.
                                mainWhitelist.add(doubleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " main white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                            }
                        } else {  // Process a main white list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blocklistEntry, originalBlocklistEntry};

                            // Add the entry to the white list.
                            mainWhitelist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " main white list added: " + blocklistEntry + "  -  " + originalBlocklistEntry);
                        }
                    }
                } else if (blocklistEntry.endsWith("|")) {  // Final black list entries.
                    // Strip out the final "|"
                    String entry = blocklistEntry.substring(0, blocklistEntry.length() - 1);

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
                        String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                        // Add the entry to the black list.
                        finalBlacklist.add(doubleEntry);

                        //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                    } else {  // Process a final black list single entry.
                        // create an entry string array.
                        String[] singleEntry = {entry, originalBlocklistEntry};

                        // Add the entry to the black list.
                        finalBlacklist.add(singleEntry);

                        //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + entry + "  -  " + originalBlocklistEntry);
                    }
                } else if (blocklistEntry.contains("$")) {  // Entries with filter options.
                    // Strip out any initial `||`.  These will be treated like any other entry.
                    if (blocklistEntry.startsWith("||")) {
                        blocklistEntry = blocklistEntry.substring(2);
                    }

                    if (blocklistEntry.contains("third-party")) {  // Third-party entries.
                        //noinspection StatementWithEmptyBody
                        if (blocklistEntry.contains("~third-party")) {  // Third-party filter white list entries.
                            // Do not process these white list entries.  They are designed to combine with block filters that Privacy Browser doesn't use, like `subdocument` and `xmlhttprequest`.

                            //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
                        } else if (blocklistEntry.contains("domain=")) {  // Third-party domain entries.
                            if (blocklistEntry.startsWith("|")) {  // Third-party domain initial entries.
                                // Strip the initial `|`.
                                blocklistEntry = blocklistEntry.substring(1);

                                // Parse the entry
                                String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                                String filters = blocklistEntry.substring(blocklistEntry.indexOf("$") + 1);
                                String domains = filters.substring(filters.indexOf("domain=") + 7);

                                //noinspection StatementWithEmptyBody
                                if (entry.equals("http:") || entry.equals("https:") || entry.equals("http://") || entry.equals("https://")) {  // Ignore generic entries.
                                    // Do nothing.  These entries will almost entirely disable the website.
                                    // Often the original entry blocks filter options like `$script`, which Privacy Browser does not differentiate.

                                    //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
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
                                                String[] tripleDomainEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                                // Add the entry to the black list.
                                                thirdPartyDomainInitialBlacklist.add(tripleDomainEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " third-party domain initial black list added: " + domain + " , " + firstEntry + " , " + realSecondEntry +
                                                //        " , " + thirdEntry + "  -  " + originalBlocklistEntry);
                                            } else {  // Process a third-party domain initial black list double entry.
                                                // Create an entry string array.
                                                String[] doubleDomainEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                                // Add the entry to the black list.
                                                thirdPartyDomainInitialBlacklist.add(doubleDomainEntry);

                                                //Log.i("Blocklists", headers.get(1)[0] + " third-party domain initial black list added: " + domain + " , " + firstEntry + " , " + secondEntry +
                                                //        "  -  " + originalBlocklistEntry);
                                            }
                                        } else {  // Process a third-party domain initial black list single entry.
                                            // Create an entry string array.
                                            String[] singleEntry = {domain, entry, originalBlocklistEntry};

                                            // Add the entry to the black list.
                                            thirdPartyDomainInitialBlacklist.add(singleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party domain initial black list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                        }
                                    } while (domains.contains("|"));
                                }
                            } else if (blocklistEntry.contains("\\")) {  // Process a third-party domain black list regular expression.
                                // Parse the entry.  At least one regular expression in this entry contains `$`, so the parser uses `/$`.
                                String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("/$") + 1);
                                String filters = blocklistEntry.substring(blocklistEntry.indexOf("/$") + 2);
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
                                    String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                    // Add the entry to the black list.
                                    thirdPartyDomainRegularExpressionBlacklist.add(domainEntry);

                                    //Log.i("Blocklists", headers.get(1)[0] + " third-party domain regular expression black list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                } while (domains.contains("|"));
                            } else {  // Third-party domain entries.
                                // Parse the entry
                                String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                                String filters = blocklistEntry.substring(blocklistEntry.indexOf("$") + 1);
                                String domains = filters.substring(filters.indexOf("domain=") + 7);

                                // Strip any trailing "*" from the entry.
                                if (entry.endsWith("*")) {
                                    entry = entry.substring(0, entry.length() - 1);
                                }

                                // Track if any third-party white list filters are applied.
                                boolean whitelistDomain = false;

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
                                        whitelistDomain = true;

                                        if (entry.contains("*")) {  // Process a third-party domain white list double entry.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhitelist.add(domainDoubleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlocklistEntry);
                                        } else {  // Process a third-party domain white list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                            // Add the entry to the white list.
                                            thirdPartyDomainWhitelist.add(domainEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party domain white list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                        }
                                    } else {  // Third-party domain black list entries.
                                        if (entry.contains("*")) {  // Process a third-party domain black list double entry.
                                            // Get the index of the wildcard.
                                            int wildcardIndex = entry.indexOf("*");

                                            // Split the entry into components.
                                            String firstEntry = entry.substring(0, wildcardIndex);
                                            String secondEntry = entry.substring(wildcardIndex + 1);

                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                            // Add the entry to the black list
                                            thirdPartyDomainBlacklist.add(domainDoubleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party domain black list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlocklistEntry);
                                        } else {  // Process a third-party domain black list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                            // Add the entry to the black list.
                                            thirdPartyDomainBlacklist.add(domainEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " third-party domain block list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                        }
                                    }
                                } while (domains.contains("|"));

                                // Add a third-party black list entry if a white list domain was processed.
                                if (whitelistDomain) {
                                    if (entry.contains("*")) {  // Process a third-party black list double entry.
                                        // Get the index of the wildcard.
                                        int wildcardIndex = entry.indexOf("*");

                                        // Split the entry into components.
                                        String firstEntry = entry.substring(0, wildcardIndex);
                                        String secondEntry = entry.substring(wildcardIndex + 1);

                                        // Create an entry string array.
                                        String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        thirdPartyBlacklist.add(doubleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                                    } else {  // Process a third-party black list single entry.
                                        // Create an entry string array.
                                        String[] singleEntry = {entry, originalBlocklistEntry};

                                        // Add an entry to the black list.
                                        thirdPartyBlacklist.add(singleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " third-party black list added: " + entry + "  -  " + originalBlocklistEntry);
                                    }
                                }
                            }
                        } else if (blocklistEntry.startsWith("|")) {  // Third-party initial black list entries.
                            // Strip the initial `|`.
                            blocklistEntry = blocklistEntry.substring(1);

                            // Get the entry.
                            String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));

                            if (entry.contains("*")) {  // Process a third-party initial black list double entry.
                                // Get the index of the wildcard.
                                int wildcardIndex = entry.indexOf("*");

                                // Split the entry into components.
                                String firstEntry = entry.substring(0, wildcardIndex);
                                String secondEntry = entry.substring(wildcardIndex + 1);

                                // Create an entry string array.
                                String[] thirdPartyDoubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                thirdPartyInitialBlacklist.add(thirdPartyDoubleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " third-party initial black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                            } else {  // Process a third-party initial black list single entry.
                                // Create an entry string array.
                                String[] singleEntry = {entry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                thirdPartyInitialBlacklist.add(singleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " third-party initial black list added: " + entry + "  -  " + originalBlocklistEntry);
                            }
                        } else if (blocklistEntry.contains("\\")) {  // Process a regular expression black list entry.
                            // Prepare a string to hold the entry.
                            String entry;

                            // Get the entry.
                            if (blocklistEntry.contains("$/$")) {  // The first `$` is part of the regular expression.
                                entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$/$") + 2);
                            } else {  // The only `$` indicates the filter options.
                                entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                            }

                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlocklistEntry};

                            // Add the entry to the black list.
                            thirdPartyRegularExpressionBlacklist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " third-party regular expression black list added: " + entry + "  -  " + originalBlocklistEntry);
                        } else if (blocklistEntry.contains("*")) {  // Third-party and regular expression black list entries.
                            // Get the entry.
                            String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));

                            if (entry.endsWith("*")) {  // Process a third-party black list single entry.
                                // Strip the final `*`.
                                entry = entry.substring(0, entry.length() - 1);

                                // Create an entry string array.
                                String[] singleEntry = {entry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                thirdPartyBlacklist.add(singleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " third party black list added: " + entry + "  -  " + originalBlocklistEntry);
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
                                        String[] quadrupleEntry = {firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        thirdPartyBlacklist.add(quadrupleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                        //        fourthEntry + "  -  " + originalBlocklistEntry);
                                    } else {  // Process a third-party black list triple entry.
                                        // Create an entry string array.
                                        String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        thirdPartyBlacklist.add(tripleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                        //        originalBlocklistEntry);
                                    }
                                } else {  // Process a third-party black list double entry.
                                    // Create an entry string array.
                                    String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                    // Add the entry to the black list.
                                    thirdPartyBlacklist.add(doubleEntry);

                                    //Log.i("Blocklists", headers.get(1)[0] + " third-party black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                                }
                            }
                        } else {  // Process a third party black list single entry.
                            // Get the entry.
                            String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));

                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlocklistEntry};

                            // Add the entry to the black list.
                            thirdPartyBlacklist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " third party black list added: " + entry + "  -  " + originalBlocklistEntry);
                        }
                    } else if (blocklistEntry.substring(blocklistEntry.indexOf("$")).contains("domain=")) {  // Domain entries.
                        if (blocklistEntry.contains("~")) {  // Domain white list entries.
                            // Separate the filters.
                            String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                            String filters = blocklistEntry.substring(blocklistEntry.indexOf("$") + 1);
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
                                        String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                        // Add the entry to the white list.
                                        domainWhitelist.add(domainTripleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " domain white list added: " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry +
                                        //        "  -  " + originalBlocklistEntry);
                                    } else {  // Process a domain white list double entry.
                                        // Create an entry string array.
                                        String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                        // Add the entry to the white list.
                                        domainWhitelist.add(domainDoubleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " domain white list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                                    }
                                } else {  // Process a domain white list single entry.
                                    // Create an entry string array.
                                    String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                    // Add the entry to the white list.
                                    domainWhitelist.add(domainEntry);

                                    //Log.i("Blocklists", headers.get(1)[0] + " domain white list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                }
                            } while (domains.contains("|"));
                        } else {  // Domain black list entries.
                            // Separate the filters.
                            String entry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                            String filters = blocklistEntry.substring(blocklistEntry.indexOf("$") + 1);
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

                                            //Log.i("Blocklists", headers.get(1)[0] + " not added: " + originalBlocklistEntry);
                                        } else {  // Process a domain initial black list entry
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entryBase, originalBlocklistEntry};

                                            // Add the entry to the black list.
                                            domainInitialBlacklist.add(domainEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " domain initial black list added: " + domain + " , " + entryBase + "  -  " + originalBlocklistEntry);
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
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                            // Add the entry to the black list.
                                            domainFinalBlacklist.add(domainDoubleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " domain final black list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlocklistEntry);
                                        } else {  // Process a domain final black list single entry.
                                            // Create an entry string array.
                                            String[] domainEntry = {domain, entryBase, originalBlocklistEntry};

                                            // Add the entry to the black list.
                                            domainFinalBlacklist.add(domainEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " domain final black list added: " + domain + " , " + entryBase + "  -  " + originalBlocklistEntry);
                                        }
                                    } else if (entry.contains("\\")) {  // Process a domain regular expression black list entry.
                                        // Create an entry string array.
                                        String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        domainRegularExpressionBlacklist.add(domainEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " domain regular expression black list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
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
                                            String[] domainTripleEntry = {domain, firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                            // Add the entry to the black list.
                                            domainBlacklist.add(domainTripleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " domain black list added: " + domain + " , " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry +
                                            //        "  -  " + originalBlocklistEntry);
                                        } else {  // Process a domain black list double entry.
                                            // Create an entry string array.
                                            String[] domainDoubleEntry = {domain, firstEntry, secondEntry, originalBlocklistEntry};

                                            // Add the entry to the black list.
                                            domainBlacklist.add(domainDoubleEntry);

                                            //Log.i("Blocklists", headers.get(1)[0] + " domain black list added: " + domain + " , " + firstEntry + " , " + secondEntry + "  -  " +
                                            //        originalBlocklistEntry);
                                        }
                                    } else {  // Process a domain black list single entry.
                                        // Create an entry string array.
                                        String[] domainEntry = {domain, entry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        domainBlacklist.add(domainEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " domain black list added: " + domain + " , " + entry + "  -  " + originalBlocklistEntry);
                                    }
                                } while (domains.contains("|"));
                            }
                        }
                    } else if (blocklistEntry.contains("~")) {  // White list entries.  Privacy Browser does not differentiate against these filter options, so they are just generally white listed.
                        // Remove the filter options.
                        blocklistEntry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));

                        // Strip any trailing `*`.
                        if (blocklistEntry.endsWith("*")) {
                            blocklistEntry = blocklistEntry.substring(0, blocklistEntry.length() - 1);
                        }

                        if (blocklistEntry.contains("*")) {  // Process a white list double entry.
                            // Get the index of the wildcard.
                            int wildcardIndex = blocklistEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blocklistEntry.substring(0, wildcardIndex);
                            String secondEntry = blocklistEntry.substring(wildcardIndex + 1);

                            // Create an entry string array.
                            String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                            // Add the entry to the white list.
                            mainWhitelist.add(doubleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " main white list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                        } else {  // Process a white list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blocklistEntry, originalBlocklistEntry};

                            // Add the entry to the white list.
                            mainWhitelist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " main white list added: " + blocklistEntry + "  -  + " + originalBlocklistEntry);
                        }
                    } else if (blocklistEntry.contains("\\")) {  // Process a regular expression black list entry.
                        // Remove the filter options.
                        blocklistEntry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));

                        // Create an entry string array.
                        String[] singleEntry = {blocklistEntry, originalBlocklistEntry};

                        // Add the entry to the black list.
                        regularExpressionBlacklist.add(singleEntry);

                        //Log.i("Blocklists", headers.get(1)[0] + " regular expression black list added: " + blocklistEntry + "  -  " + originalBlocklistEntry);
                    } else {  // Black list entries.
                        // Remove the filter options.
                        if (!blocklistEntry.contains("$file")) {  // EasyPrivacy contains an entry with `$file` that does not have filter options.
                            blocklistEntry = blocklistEntry.substring(0, blocklistEntry.indexOf("$"));
                        }

                        // Strip any trailing `*`.  These are redundant.
                        if (blocklistEntry.endsWith("*")) {
                            blocklistEntry = blocklistEntry.substring(0, blocklistEntry.length() - 1);
                        }

                        if (blocklistEntry.startsWith("|")) {  // Initial black list entries.
                            // Strip the initial `|`.
                            String entry = blocklistEntry.substring(1);

                            if (entry.contains("*")) {  // Process an initial black list double entry.
                                // Get the index of the wildcard.
                                int wildcardIndex = entry.indexOf("*");

                                // Split the entry into components.
                                String firstEntry = entry.substring(0, wildcardIndex);
                                String secondEntry = entry.substring(wildcardIndex + 1);

                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                initialBlacklist.add(doubleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " initial black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                            } else {  // Process an initial black list single entry.
                                // Create an entry string array.
                                String[] singleEntry = {entry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                initialBlacklist.add(singleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " initial black list added: " + entry + "  -  " + originalBlocklistEntry);
                            }
                        } else if (blocklistEntry.endsWith("|")) {  // Final black list entries.
                            // Ignore entries with `object` filters.  They can block entire websites and don't have any meaning in the context of Privacy Browser.
                            if (!originalBlocklistEntry.contains("$object")) {
                                // Strip the final `|`.
                                String entry = blocklistEntry.substring(0, blocklistEntry.length() - 1);

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
                                        String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        finalBlacklist.add(tripleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                        //        originalBlocklistEntry);
                                    } else {  // Process a final black list double entry.
                                        // Create an entry string array.
                                        String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        finalBlacklist.add(doubleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                                    }
                                } else {  // Process a final black list single entry.
                                    // Create an entry sting array.
                                    String[] singleEntry = {entry, originalBlocklistEntry};

                                    // Add the entry to the black list.
                                    finalBlacklist.add(singleEntry);

                                    //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + entry + "  -  " + originalBlocklistEntry);
                                }
                            }
                        } else if (blocklistEntry.contains("*")) {  // There are two or more entries.
                            // Get the index of the wildcard.
                            int wildcardIndex = blocklistEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blocklistEntry.substring(0, wildcardIndex);
                            String secondEntry = blocklistEntry.substring(wildcardIndex + 1);

                            if (secondEntry.contains("*")) {  // Process a main black list triple entry.
                                // Get the index of the wildcard.
                                int secondWildcardIndex = secondEntry.indexOf("*");

                                // Split the entry into components.
                                String realSecondEntry = secondEntry.substring(0, secondWildcardIndex);
                                String thirdEntry = secondEntry.substring(secondWildcardIndex + 1);

                                // Create an entry string array.
                                String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                mainBlacklist.add(tripleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " + originalBlocklistEntry);
                            } else {  // Process a main black list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                mainBlacklist.add(doubleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                            }
                        } else {  // Process a main black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blocklistEntry, originalBlocklistEntry};

                            // Add the entry to the black list.
                            mainBlacklist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + blocklistEntry + "  -  " + originalBlocklistEntry);
                        }
                    }
                } else {  // Main black list entries
                    // Strip out any initial `||`.  These will be treated like any other entry.
                    if (blocklistEntry.startsWith("||")) {
                        blocklistEntry = blocklistEntry.substring(2);
                    }

                    // Strip out any initial `*`.
                    if (blocklistEntry.startsWith("*")) {
                        blocklistEntry = blocklistEntry.substring(1);
                    }

                    // Strip out any trailing `*`.
                    if (blocklistEntry.endsWith("*")) {
                        blocklistEntry = blocklistEntry.substring(0, blocklistEntry.length() - 1);
                    }

                    if (blocklistEntry.startsWith("|")) {  // Initial black list entries.
                        // Strip the initial `|`.
                        String entry = blocklistEntry.substring(1);

                        if (entry.contains("*")) {  // Process an initial black list double entry.
                            // Get the index of the wildcard.
                            int wildcardIndex = entry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = entry.substring(0, wildcardIndex);
                            String secondEntry = entry.substring(wildcardIndex + 1);

                            // Create an entry string array.
                            String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                            // Add the entry to the black list.
                            initialBlacklist.add(doubleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " initial black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                        } else {  // Process an initial black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlocklistEntry};

                            // Add the entry to the black list.
                            initialBlacklist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " initial black list added: " + entry + "  -  " + originalBlocklistEntry);
                        }
                    } else if (blocklistEntry.endsWith("|")) {  // Final black list entries.
                        // Strip the final `|`.
                        String entry = blocklistEntry.substring(0, blocklistEntry.length() - 1);

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
                                String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                finalBlacklist.add(tripleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " +
                                //        originalBlocklistEntry);
                            } else {  // Process a final black list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                finalBlacklist.add(doubleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                            }
                        } else {  // Process a final black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {entry, originalBlocklistEntry};

                            // Add the entry to the black list.
                            finalBlacklist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " final black list added: " + entry + "  -  " + originalBlocklistEntry);
                        }
                    } else {  // Main black list entries.
                        if (blocklistEntry.contains("*")) {  // There are two or more entries.
                            // Get the index of the wildcard.
                            int wildcardIndex = blocklistEntry.indexOf("*");

                            // Split the entry into components.
                            String firstEntry = blocklistEntry.substring(0, wildcardIndex);
                            String secondEntry = blocklistEntry.substring(wildcardIndex + 1);

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
                                        String[] quintupleEntry = {firstEntry, realSecondEntry, realThirdEntry, realFourthEntry, fifthEntry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        mainBlacklist.add(quintupleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                        //        realFourthEntry + " , " + fifthEntry + "  -  " + originalBlocklistEntry);
                                    } else {  // Process a main black list quadruple entry.
                                        // Create an entry string array.
                                        String[] quadrupleEntry = {firstEntry, realSecondEntry, realThirdEntry, fourthEntry, originalBlocklistEntry};

                                        // Add the entry to the black list.
                                        mainBlacklist.add(quadrupleEntry);

                                        //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + realThirdEntry + " , " +
                                        //        fourthEntry + "  -  " + originalBlocklistEntry);
                                    }
                                } else {  // Process a main black list triple entry.
                                    // Create an entry string array.
                                    String[] tripleEntry = {firstEntry, realSecondEntry, thirdEntry, originalBlocklistEntry};

                                    // Add the entry to the black list.
                                    mainBlacklist.add(tripleEntry);

                                    //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + realSecondEntry + " , " + thirdEntry + "  -  " + originalBlocklistEntry);
                                }
                            } else {  // Process a main black list double entry.
                                // Create an entry string array.
                                String[] doubleEntry = {firstEntry, secondEntry, originalBlocklistEntry};

                                // Add the entry to the black list.
                                mainBlacklist.add(doubleEntry);

                                //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + firstEntry + " , " + secondEntry + "  -  " + originalBlocklistEntry);
                            }
                        } else {  // Process a main black list single entry.
                            // Create an entry string array.
                            String[] singleEntry = {blocklistEntry, originalBlocklistEntry};

                            // Add the entry to the black list.
                            mainBlacklist.add(singleEntry);

                            //Log.i("Blocklists", headers.get(1)[0] + " main black list added: " + blocklistEntry + "  -  " + originalBlocklistEntry);
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
        combinedLists.add(mainWhitelist);  // 1.
        combinedLists.add(finalWhitelist);  // 2.
        combinedLists.add(domainWhitelist);  // 3.
        combinedLists.add(domainInitialWhitelist);  // 4.
        combinedLists.add(domainFinalWhitelist); // 5.
        combinedLists.add(thirdPartyWhitelist);  // 6.
        combinedLists.add(thirdPartyDomainWhitelist);  // 7.
        combinedLists.add(thirdPartyDomainInitialWhitelist);  // 8.

        // Add the black lists (9-22).
        combinedLists.add(mainBlacklist);  // 9.
        combinedLists.add(initialBlacklist);  // 10.
        combinedLists.add(finalBlacklist);  // 11.
        combinedLists.add(domainBlacklist);  //  12.
        combinedLists.add(domainInitialBlacklist);  // 13.
        combinedLists.add(domainFinalBlacklist);  // 14.
        combinedLists.add(domainRegularExpressionBlacklist);  // 15.
        combinedLists.add(thirdPartyBlacklist);  // 16.
        combinedLists.add(thirdPartyInitialBlacklist);  // 17.
        combinedLists.add(thirdPartyDomainBlacklist);  // 18.
        combinedLists.add(thirdPartyDomainInitialBlacklist);  // 19.
        combinedLists.add(thirdPartyRegularExpressionBlacklist);  // 20.
        combinedLists.add(thirdPartyDomainRegularExpressionBlacklist);  // 21.
        combinedLists.add(regularExpressionBlacklist);  // 22.

        return combinedLists;
    }

    public String[] checkBlocklist(String currentDomain, String resourceUrl, boolean isThirdPartyRequest, ArrayList<List<String[]>> blocklist) {
        // Get the blocklist name.
        String BLOCK_LIST_NAME_STRING = blocklist.get(0).get(1)[0];

        // Assert that currentDomain != null only if this is a third party request.  Apparently, lint can't tell that this isn't redundant.
        //noinspection RedundantIfStatement
        if (isThirdPartyRequest) {
            assert currentDomain != null;
        }

        // Process the white lists.
        // Main white list.
        for (String[] whitelistEntry : blocklist.get(Integer.valueOf(MAIN_WHITELIST))) {
            switch (whitelistEntry.length) {
                case 2:  // There is one entry.
                    if (resourceUrl.contains(whitelistEntry[0])) {
                        // Return a whitelist match request allowed.
                        return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_WHITELIST, whitelistEntry[0], whitelistEntry[1]};
                    }
                    break;

                case 3:  // There are two entries.
                    if (resourceUrl.contains(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1])) {
                        // Return a whitelist match request allowed.
                        return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                    }
                    break;

                case 4:  // There are three entries.
                    if (resourceUrl.contains(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2])) {
                        // Return a whitelist match request allowed.
                        return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2], whitelistEntry[3]};
                    }
                    break;
            }
        }

        // Final white list.
        for (String[] whitelistEntry : blocklist.get(Integer.valueOf(FINAL_WHITELIST))) {
            if (whitelistEntry.length == 2) {  // There is one entry.
                if (resourceUrl.contains(whitelistEntry[0])) {
                    // Return a whitelist match request allowed.
                    return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, FINAL_WHITELIST, whitelistEntry[0], whitelistEntry[1]};
                }
            } else {  // There are two entries.
                if (resourceUrl.contains(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1])) {
                    // Return a whitelist match request allowed.
                    return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, FINAL_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                }
            }
        }

        // Only check the domain lists if the current domain is not null (like `about:blank`).
        if (currentDomain != null) {
            // Domain white list.
            for (String[] whitelistEntry : blocklist.get(Integer.valueOf(DOMAIN_WHITELIST))) {
                switch (whitelistEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2],
                                    whitelistEntry[3]};
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2]) && resourceUrl.contains(whitelistEntry[3])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2] + "\n" +
                                    whitelistEntry[3], whitelistEntry[4]};
                        }
                        break;

                    case 6:  // There are four entries.
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2]) && resourceUrl.contains(whitelistEntry[3]) &&
                                resourceUrl.contains(whitelistEntry[4])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2] + "\n" +
                                    whitelistEntry[3] + "\n" + whitelistEntry[4], whitelistEntry[5]};
                        }
                        break;
                }
            }

            // Domain initial white list.
            for (String[] whitelistEntry : blocklist.get(Integer.valueOf(DOMAIN_INITIAL_WHITELIST))) {
                switch (whitelistEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.startsWith(whitelistEntry[1])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_INITIAL_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.startsWith(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_INITIAL_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2],
                                    whitelistEntry[3]};
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.startsWith(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2]) && resourceUrl.startsWith(whitelistEntry[3])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_INITIAL_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2] + "\n" +
                                    whitelistEntry[3], whitelistEntry[4]};
                        }
                        break;
                }
            }

            // Domain final white list.
            for (String[] whitelistEntry : blocklist.get(Integer.valueOf(DOMAIN_FINAL_WHITELIST))) {
                switch (whitelistEntry.length) {
                    case 3:  // There is one entry;
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.endsWith(whitelistEntry[1])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_FINAL_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                        }
                        break;

                    case 4:  // There are two entries;
                        if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.endsWith(whitelistEntry[2])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_FINAL_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2],
                                    whitelistEntry[3]};
                        }
                        break;
                }
            }
        }

        // Only check the third-party white lists if this is a third-party request.
        if (isThirdPartyRequest) {
            // Third-party white list.
            for (String[] whitelistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_WHITELIST))) {
                switch (whitelistEntry.length) {
                    case 2:  // There is one entry
                        if (resourceUrl.contains(whitelistEntry[0])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_WHITELIST, whitelistEntry[0], whitelistEntry[1]};
                        }
                        break;

                    case 3:  // There are two entries.
                        if (resourceUrl.contains(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                        }
                        break;

                    case 4:  // There are three entries.
                        if (resourceUrl.contains(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2],
                                    whitelistEntry[3]};
                        }
                        break;

                    case 5:  // There are four entries.
                        if (resourceUrl.contains(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2]) && resourceUrl.contains(whitelistEntry[3])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2] + "\n" +
                                    whitelistEntry[3], whitelistEntry[4]};
                        }
                        break;

                    case 6:  // There are five entries.
                        if (resourceUrl.contains(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2]) && resourceUrl.contains(whitelistEntry[3]) &&
                                resourceUrl.contains(whitelistEntry[4])) {
                            // Return a whitelist match request allowed.
                            return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2] + "\n" +
                                    whitelistEntry[3] + "\n" + whitelistEntry[4], whitelistEntry[5]};
                        }
                        break;
                }
            }

            // Third-party domain white list.
            for (String[] whitelistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_DOMAIN_WHITELIST))) {
                if (whitelistEntry.length == 3) {  // There is one entry.
                    if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1])) {
                        // Return a whitelist match request allowed.
                        return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                    }
                } else {  // There are two entries.
                    if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.contains(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2])) {
                        // Return a whitelist match request allowed.
                        return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2],
                                whitelistEntry[3]};
                    }
                }
            }

            // Third-party domain initial white list.
            for (String[] whitelistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_DOMAIN_INITIAL_WHITELIST))) {
                if (whitelistEntry.length == 3) {  // There is one entry.
                    if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.startsWith(whitelistEntry[1])) {
                        // Return a whitelist match request allowed.
                        return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_INITIAL_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1], whitelistEntry[2]};
                    }
                } else {  // There are two entries.
                    if (currentDomain.endsWith(whitelistEntry[0]) && resourceUrl.startsWith(whitelistEntry[1]) && resourceUrl.contains(whitelistEntry[2])) {
                        // Return a whitelist match request allowed.
                        return new String[] {REQUEST_ALLOWED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_WHITELIST, whitelistEntry[0] + "\n" + whitelistEntry[1] + "\n" + whitelistEntry[2],
                                whitelistEntry[3]};
                    }
                }
            }
        }

        // Process the black lists.
        // Main black list.
        for (String[] blacklistEntry : blocklist.get(Integer.valueOf(MAIN_BLACKLIST))) {
            switch (blacklistEntry.length) {
                case 2:  // There is one entry.
                    if (resourceUrl.contains(blacklistEntry[0])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_BLACKLIST, blacklistEntry[0], blacklistEntry[1]};
                    }
                    break;

                case 3:  // There are two entries.
                    if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                    }
                    break;

                case 4:  // There are three entries.
                    if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2], blacklistEntry[3]};
                    }
                    break;

                case 5:  // There are four entries.
                    if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2]) && resourceUrl.contains(blacklistEntry[3])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2] + "\n" +
                                blacklistEntry[3], blacklistEntry[4]};
                    }
                    break;

                case 6:  // There are five entries.
                    if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2]) && resourceUrl.contains(blacklistEntry[3]) &&
                            resourceUrl.contains(blacklistEntry[4])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, MAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2] + "\n" +
                                blacklistEntry[3] + "\n" + blacklistEntry[4], blacklistEntry[5]};
                    }
                    break;
            }
        }

        // Initial black list.
        for (String[] blacklistEntry : blocklist.get(Integer.valueOf(INITIAL_BLACKLIST))) {
            if (blacklistEntry.length == 2) {  // There is one entry.
                if (resourceUrl.startsWith(blacklistEntry[0])) {
                    // Return a blacklist match request blocked.
                    return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, INITIAL_BLACKLIST, blacklistEntry[0], blacklistEntry[1]};
                }
            } else {  // There are two entries
                if (resourceUrl.startsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1])) {
                    // Return a blacklist match request blocked.
                    return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, INITIAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                }
            }
        }

        // Final black list.
        for (String[] blacklistEntry : blocklist.get(Integer.valueOf(FINAL_BLACKLIST))) {
            switch (blacklistEntry.length) {
                case 2:  // There is one entry.
                    if (resourceUrl.endsWith(blacklistEntry[0])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, FINAL_BLACKLIST, blacklistEntry[0], blacklistEntry[1]};
                    }
                    break;

                case 3:  // There are two entries.
                    if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.endsWith(blacklistEntry[1])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, FINAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                    }
                    break;

                case 4:  // There are three entries.
                    if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.endsWith(blacklistEntry[2])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, FINAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2], blacklistEntry[3]};
                    }
                    break;
            }
        }

        // Only check the domain lists if the current domain is not null (like `about:blank`).
        if (currentDomain != null) {
            // Domain black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(DOMAIN_BLACKLIST))) {
                switch (blacklistEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2],
                                    blacklistEntry[3]};
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2]) && resourceUrl.contains(blacklistEntry[3])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2] + "\n" +
                                    blacklistEntry[3], blacklistEntry[4]};
                        }
                        break;
                }
            }

            // Domain initial black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(DOMAIN_INITIAL_BLACKLIST))) {
                // Store the entry in the resource request log.
                if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.startsWith(blacklistEntry[1])) {
                    // Return a blacklist match request blocked.
                    return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_INITIAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                }
            }

            // Domain final black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(DOMAIN_FINAL_BLACKLIST))) {
                switch (blacklistEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.endsWith(blacklistEntry[1])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_FINAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.endsWith(blacklistEntry[2])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_FINAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2],
                                    blacklistEntry[3]};
                        }
                        break;
                }
            }

            // Domain regular expression black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(DOMAIN_REGULAR_EXPRESSION_BLACKLIST))) {
                if (currentDomain.endsWith(blacklistEntry[0]) && Pattern.matches(blacklistEntry[1], resourceUrl)) {
                    // Return a blacklist match request blocked.
                    return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, DOMAIN_REGULAR_EXPRESSION_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                }
            }
        }

        // Only check the third-party black lists if this is a third-party request.
        if (isThirdPartyRequest) {
            // Third-party black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_BLACKLIST))) {
                switch (blacklistEntry.length) {
                    case 2:  // There is one entry.
                        if (resourceUrl.contains(blacklistEntry[0])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_BLACKLIST, blacklistEntry[0], blacklistEntry[1]};
                        }
                        break;

                    case 3:  // There are two entries.
                        if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                        }
                        break;

                    case 4:  // There are three entries.
                        if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2],
                                    blacklistEntry[3]};
                        }
                        break;

                    case 5:  // There are four entries.
                        if (resourceUrl.contains(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2]) && resourceUrl.contains(blacklistEntry[3])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2] + "\n" +
                                    blacklistEntry[3], blacklistEntry[4]};
                        }
                        break;
                }
            }

            // Third-party initial black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_INITIAL_BLACKLIST))) {
                if (blacklistEntry.length == 2) {  // There is one entry.
                    if (resourceUrl.startsWith(blacklistEntry[0])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_INITIAL_BLACKLIST, blacklistEntry[0], blacklistEntry[1]};
                    }
                } else {  // There are two entries.
                    if (resourceUrl.startsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_INITIAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                    }
                }
            }

            // Third-party domain black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_DOMAIN_BLACKLIST))) {
                if (blacklistEntry.length == 3) {  // There is one entry.
                    if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                    }
                } else { // There are two entries.
                    if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.contains(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2])) {
                        // Return a blacklist match request blocked.
                        return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" + blacklistEntry[2],
                                blacklistEntry[3]};
                    }
                }
            }

            // Third-party domain initial black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST))) {
                switch (blacklistEntry.length) {
                    case 3:  // There is one entry.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.startsWith(blacklistEntry[1])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                        }
                        break;

                    case 4:  // There are two entries.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.startsWith(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" +
                                    blacklistEntry[2], blacklistEntry[3]};
                        }
                        break;

                    case 5:  // There are three entries.
                        if (currentDomain.endsWith(blacklistEntry[0]) && resourceUrl.startsWith(blacklistEntry[1]) && resourceUrl.contains(blacklistEntry[2]) && resourceUrl.contains(blacklistEntry[3])) {
                            // Return a blacklist match request blocked.
                            return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1] + "\n" +
                                    blacklistEntry[2] + "\n" + blacklistEntry[3], blacklistEntry[4]};
                        }
                        break;
                }
            }

            // Third-party regular expression black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_REGULAR_EXPRESSION_BLACKLIST))) {
                if (Pattern.matches(blacklistEntry[0], resourceUrl)) {
                    // Return a blacklist match request blocked.
                    return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_REGULAR_EXPRESSION_BLACKLIST, blacklistEntry[0], blacklistEntry[1]};
                }
            }

            // Third-party domain regular expression black list.
            for (String[] blacklistEntry : blocklist.get(Integer.valueOf(THIRD_PARTY_DOMAIN_REGULAR_EXPRESSION_BLACKLIST))) {
                if (currentDomain.endsWith(blacklistEntry[0]) && Pattern.matches(blacklistEntry[1], resourceUrl)) {
                    // Return a blacklist match request blocked.
                    return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, THIRD_PARTY_DOMAIN_REGULAR_EXPRESSION_BLACKLIST, blacklistEntry[0] + "\n" + blacklistEntry[1], blacklistEntry[2]};
                }
            }
        }

        // Regular expression black list.
        for (String[] blacklistEntry : blocklist.get(Integer.valueOf(REGULAR_EXPRESSION_BLACKLIST))) {
            if (Pattern.matches(blacklistEntry[0], resourceUrl)) {
                // Return a blacklist match request blocked.
                return new String[] {REQUEST_BLOCKED, resourceUrl, BLOCK_LIST_NAME_STRING, REGULAR_EXPRESSION_BLACKLIST, blacklistEntry[0], blacklistEntry[1]};
            }
        }

        // Return a no match request default.
        return new String[] {REQUEST_DEFAULT};
    }
}