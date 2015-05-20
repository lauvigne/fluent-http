/**
 * Copyright (C) 2013-2015 all@code-story.net
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
package net.codestory.http.templating.helpers;

import com.github.jknack.handlebars.*;
import net.codestory.http.compilers.*;
import net.codestory.http.io.*;
import net.codestory.http.misc.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.function.*;

import static java.util.stream.Collectors.*;

public class AssetsHelperSource {
  private final Resources resources;
  private final CompilerFacade compilers;
  private final Function<String, String> urlSupplier;
  private final boolean prodMode;

  public AssetsHelperSource(boolean prodMode, Resources resources, CompilerFacade compilers) {
    this.resources = resources;
    this.compilers = compilers;
    this.prodMode = prodMode;
    this.urlSupplier = prodMode ? new Cache<>(this::uriWithSha1) : this::uriWithSha1;
  }

  // Handler entry points

  public CharSequence script(Object context, Options options) {
    String attributes = HelperTools.hashAsString(options);

    //TODO concatenate
    return HelperTools.toString(context, value -> singleScript(value.toString(), attributes));
  }

  public CharSequence css(Object context, Options options) {
    if (isConcatenate(options)) {
      return concatenate(context, options, ".css");
    } else {
      String attributes = HelperTools.hashAsString(options);

      return HelperTools.toString(context, value -> singleCss(value.toString(), attributes));
    }
  }

  // Internal
  private CharSequence concatenate(Object context, Options options, String extension) {
    String content = HelperTools.contextAsList(context).stream()
      .map(value -> getContent(value.toString(), extension))
      .collect(joining("\n"));

    String concatenateFileName = addExtensionIfMissing(options.hash("concatenate"), extension);
    SourceFile sourceFile = new SourceFile(Paths.get(concatenateFileName), content);
    //Add the concatenate content in the cache
    //compilers.compile(sourceFile); //Not works
    //How do it ?

    //TODO get attributes without concatenate
    String attributes = HelperTools.hashAsString(options);

    String uri = Paths.get(concatenateFileName).toString();
    return new Handlebars.SafeString(singleCss(uri, attributes));
  }

  private String getContent(String value, String extension) {
    byte[] bytes = getBytes(addExtensionIfMissing(value, extension));
    return new String(bytes, Charset.defaultCharset());
  }

  private boolean isConcatenate(Options options) {
    return prodMode && options != null && options.hash("concatenate") != null;
  }

  private String uriWithSha1(String uri) {
    return uriWithSha1(uri, getBytes(uri));
  }

  private String uriWithSha1(String uri, byte[] content) {
    if (content == null) {
      return uri;
    } else {
      return uri + '?' + Sha1.of(content);
      }
  }

  private byte[] getBytes(String uri) {
    try {
      Path path = resources.findExistingPath(uri);
      if ((path != null) && (resources.isPublic(path))) {
        return resources.readBytes(path);
      }

      Path sourcePath = compilers.findPublicSourceFor(uri);
      if (sourcePath != null) {
        return resources.readBytes(sourcePath);
      }

      return null;
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read the content of : " + uri, e);
    }
  }

  private CharSequence singleScript(String path, String attributes) {
    String uri = addExtensionIfMissing(path, ".js");

    return "<script src=\"" + urlSupplier.apply(uri) + "\"" + attributes + "></script>";
  }

  private CharSequence singleCss(String path, String attributes) {
    String uri = addExtensionIfMissing(path, ".css");

    return "<link rel=\"stylesheet\" href=\"" + urlSupplier.apply(uri) + "\"" + attributes + ">";
  }

  private static String addExtensionIfMissing(String uri, String extension) {
    return uri.endsWith(extension) ? uri : uri + extension;
  }
}
