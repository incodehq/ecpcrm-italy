/*
 *  Copyright 2014 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.incode.eurocommercial.relatio.module.base.seed.users;

import java.util.Arrays;

import org.isisaddons.module.security.dom.user.AccountType;
import org.isisaddons.module.security.seed.scripts.AbstractUserAndRolesFixtureScript;
import org.isisaddons.module.security.seed.scripts.IsisModuleSecurityAdminRoleAndPermissions;

import org.incode.eurocommercial.relatio.module.base.seed.roles.AuditModuleRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.CommandModuleRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.DevUtilsModuleRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.RelatioFixtureServiceRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.RelatioRegularRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.PublishingModuleRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.SessionLoggerModuleRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.SettingsModuleRoleAndPermissions;
import org.incode.eurocommercial.relatio.module.base.seed.roles.TranslationServicePoMenuRoleAndPermissions;

public class RelatioAdminUser extends AbstractUserAndRolesFixtureScript {

    public static final String USER_NAME = "relatio-admin";
    public static final String TENANCY_PATH = "/";

    private static final String PASSWORD = "pass";

    public RelatioAdminUser() {
        super(USER_NAME, PASSWORD, null,
                TENANCY_PATH, AccountType.LOCAL,
                Arrays.asList(IsisModuleSecurityAdminRoleAndPermissions.ROLE_NAME,
                              AuditModuleRoleAndPermissions.ROLE_NAME,
                              CommandModuleRoleAndPermissions.ROLE_NAME,
                              SessionLoggerModuleRoleAndPermissions.ROLE_NAME,
                              SettingsModuleRoleAndPermissions.ROLE_NAME,
                              PublishingModuleRoleAndPermissions.ROLE_NAME,
                              DevUtilsModuleRoleAndPermissions.ROLE_NAME,
                              RelatioRegularRoleAndPermissions.ROLE_NAME,
                              RelatioFixtureServiceRoleAndPermissions.ROLE_NAME,
                              TranslationServicePoMenuRoleAndPermissions.ROLE_NAME
                        ));
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        super.execute(executionContext);
    }

}
