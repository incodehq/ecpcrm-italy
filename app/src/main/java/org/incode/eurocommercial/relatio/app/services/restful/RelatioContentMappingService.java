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
package org.incode.eurocommercial.relatio.app.services.restful;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Joiner;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.conmap.ContentMappingService;

import org.incode.eurocommercial.relatio.app.services.restful.v1.ProfileDtoFactory;
import org.incode.eurocommercial.relatio.dom.profile.Profile;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class RelatioContentMappingService implements ContentMappingService {

    @Programmatic
    @Override
    public Object map(
            final Object object,
            final List<MediaType> acceptableMediaTypes) {

        final String domainType = determineDomainType(acceptableMediaTypes);

        if (object instanceof Profile) {
            return profileDtoFactory.newDto((Profile) object);
        }

        return null;
    }

    static String determineDomainType(final List<MediaType> acceptableMediaTypes) {
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            final Map<String, String> parameters = acceptableMediaType.getParameters();
            final String domainType = parameters.get("x-ro-domain-type");
            if(domainType != null) {
                return domainType;
            }
        }
        throw new IllegalArgumentException(
                "Could not locate x-ro-domain-type parameter in any of the provided media types; got: " + Joiner.on(", ").join(acceptableMediaTypes));
    }

    @Inject private ProfileDtoFactory profileDtoFactory;
}
