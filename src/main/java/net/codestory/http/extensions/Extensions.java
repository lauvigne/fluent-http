/**
 * Copyright (C) 2013 all@code-story.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.codestory.http.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.codestory.http.Context;
import net.codestory.http.Request;
import net.codestory.http.Response;
import net.codestory.http.compilers.CompilerFacade;
import net.codestory.http.injection.IocAdapter;
import net.codestory.http.misc.Env;
import net.codestory.http.payload.PayloadWriter;
import net.codestory.http.templating.Site;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

public interface Extensions extends Serializable {
  Extensions DEFAULT = new Extensions() {
  };

  /**
   * Called each time routes are loaded.
   */
  public default ObjectMapper configureOrReplaceObjectMapper(ObjectMapper defaultObjectMapper, Env env) {
    return defaultObjectMapper;
  }

  /**
   * Called each time routes are loaded.
   */
  public default void configureCompilers(CompilerFacade compilers, Env env) {
  }

  /**
   * Called each request.
   */
  public default Context createContext(Request request, Response response, IocAdapter iocAdapter, Site site) {
    return new Context(request, response, iocAdapter, site);
  }

  /**
   * Called each request.
   */
  public default PayloadWriter createPayloadWriter(Request request, Response response, Env env, Site site, CompilerFacade compilers, ExecutorService executorService) {
    return new PayloadWriter(request, response, env, site, compilers, executorService);
  }
}
