/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
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
package org.incode.eurocommercial.relatio.app.services.homepage;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import org.isisaddons.wicket.wickedcharts.cpt.applib.WickedChart;

@DomainService(
        nature = NatureOfService.VIEW_CONTRIBUTIONS_ONLY // trick to suppress the actions from the top-level menu
)
@DomainObject(
        objectType = "HomePageService"
)
public class HomePageService {

    //region > homePage (action)

    @Action(
            semantics = SemanticsOf.SAFE
    )
    @HomePage
    public WickedChart homePage() {
        final HomePageViewModel vm = serviceRegistry.injectServicesInto(new HomePageViewModel());
        return vm.getChart();
    }

    //endregion

    //region > injected services

    @javax.inject.Inject
    ServiceRegistry serviceRegistry;

    //endregion
}
