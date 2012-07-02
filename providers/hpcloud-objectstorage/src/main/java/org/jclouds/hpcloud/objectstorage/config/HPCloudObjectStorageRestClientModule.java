/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.hpcloud.objectstorage.config;

import static org.jclouds.util.Suppliers2.getLastValueInMap;

import java.net.URI;
import java.util.Map;

import javax.inject.Singleton;

import org.jclouds.hpcloud.objectstorage.HPCloudObjectStorageAsyncClient;
import org.jclouds.hpcloud.objectstorage.HPCloudObjectStorageClient;
import org.jclouds.hpcloud.objectstorage.extensions.HPCloudCDNAsyncClient;
import org.jclouds.hpcloud.objectstorage.extensions.HPCloudCDNClient;
import org.jclouds.hpcloud.services.HPExtensionCDN;
import org.jclouds.hpcloud.services.HPExtensionServiceType;
import org.jclouds.location.suppliers.ImplicitLocationSupplier;
import org.jclouds.location.suppliers.LocationsSupplier;
import org.jclouds.location.suppliers.RegionIdToURISupplier;
import org.jclouds.location.suppliers.all.RegionToProvider;
import org.jclouds.location.suppliers.implicit.FirstRegion;
import org.jclouds.openstack.swift.CommonSwiftAsyncClient;
import org.jclouds.openstack.swift.CommonSwiftClient;
import org.jclouds.openstack.swift.config.SwiftRestClientModule;
import org.jclouds.rest.ConfiguresRestClient;
import org.jclouds.rest.annotations.ApiVersion;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/**
 * 
 * @author Adrian Cole
 */
@ConfiguresRestClient
public class HPCloudObjectStorageRestClientModule extends
         SwiftRestClientModule<HPCloudObjectStorageClient, HPCloudObjectStorageAsyncClient> {
   public static final Map<Class<?>, Class<?>> DELEGATE_MAP = ImmutableMap.<Class<?>, Class<?>> builder().put(
            HPCloudCDNClient.class, HPCloudCDNAsyncClient.class).build();

   public HPCloudObjectStorageRestClientModule() {
      super(TypeToken.of(HPCloudObjectStorageClient.class), TypeToken.of(HPCloudObjectStorageAsyncClient.class),
               DELEGATE_MAP);
   }

   protected void bindResolvedClientsToCommonSwift() {
      bind(CommonSwiftClient.class).to(HPCloudObjectStorageClient.class).in(Scopes.SINGLETON);
      bind(CommonSwiftAsyncClient.class).to(HPCloudObjectStorageAsyncClient.class).in(Scopes.SINGLETON);
   }
   
   @Override
   protected void installLocations() {
      super.installLocations();
      bind(ImplicitLocationSupplier.class).to(FirstRegion.class).in(Scopes.SINGLETON);
      bind(LocationsSupplier.class).to(RegionToProvider.class).in(Scopes.SINGLETON);
   }
   
   @Provides
   @Singleton
   @HPExtensionCDN
   protected Supplier<URI> provideCDNUrl(RegionIdToURISupplier.Factory factory, @ApiVersion String apiVersion) {
      return getLastValueInMap(factory.createForApiTypeAndVersion(HPExtensionServiceType.CDN, apiVersion));
   }
}