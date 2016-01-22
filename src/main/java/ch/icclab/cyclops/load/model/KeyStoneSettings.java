/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.load.model;

/**
 * Author: Martin Skoviera
 * Created on: 16-Nov-15
 * Description: Settings for KeyStone
 */
public class KeyStoneSettings{
    private String KeystoneURL;
    private String KeystoneUsername;
    private String KeystonePassword;
    private String KeystoneTenantName;
    private String CeilometerURL;

    /**
     * Simple constructor saving all provided information
     */
    public KeyStoneSettings(String keystoneURL, String keystoneUsername, String keystonePassword, String keystoneTenantName, String ceilometerURL) {
        KeystoneURL = keystoneURL;
        KeystoneUsername = keystoneUsername;
        KeystonePassword = keystonePassword;
        KeystoneTenantName = keystoneTenantName;
        CeilometerURL = ceilometerURL;
    }

    //======== We only need getters
    public String getKeystoneURL() {
        return KeystoneURL;
    }

    public String getKeystoneUsername() {
        return KeystoneUsername;
    }

    public String getKeystonePassword() {
        return KeystonePassword;
    }

    public String getKeystoneTenantName() {
        return KeystoneTenantName;
    }

    public String getCeilometerURL() {
        return CeilometerURL;
    }
}
