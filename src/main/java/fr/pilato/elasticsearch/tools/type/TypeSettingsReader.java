/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.tools.type;

import fr.pilato.elasticsearch.tools.SettingsFinder.Defaults;
import fr.pilato.elasticsearch.tools.SettingsReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * By default, indexes are created with their default Elasticsearch settings. You can specify
 * your own settings for your index by putting a /es/indexname/_settings.json in your classpath.
 * <br>
 * So if you create a file named /es/twitter/_settings.json in your src/main/resources folder (for maven lovers),
 * it will be used by the factory to create the twitter index.
 * <pre>
 * {@code
 * {
 *   "index" : {
 *     "number_of_shards" : 3,
 *     "number_of_replicas" : 2
 *   }
 * }
 * </pre>
 * By default, types are not created and wait for the first document you send to Elasticsearch (auto mapping).
 * But, if you define a file named /es/indexname/type.json in your classpath, the type will be created at startup using
 * the type definition you give.
 * <br>
 * So if you create a file named /es/twitter/tweet.json in your src/main/resources folder (for maven lovers),
 * it will be used by the factory to create the tweet type in twitter index.
 * <pre>
 * {@code
 * {
 *   "tweet" : {
 *     "properties" : {
 *       "message" : {"type" : "string", "store" : "yes"}
 *     }
 *   }
 * }
 * </pre>
 *
 * By convention, the factory will create all settings and mappings found under the /es classpath.<br>
 * You can disable convention and use configuration by setting autoscan to false.
 * @author David Pilato
 */
public class TypeSettingsReader extends SettingsReader {

	private static final Logger logger = LogManager.getLogger(TypeSettingsReader.class);

	/**
	 * Read a mapping
	 * @param root dir within the classpath
	 * @param index index name
	 * @param type type name (.json will be appended)
	 */
	public static String readMapping(String root, String index, String type) throws IOException {
		if (root == null) {
			return readMapping(index, type);
		}
		String mappingFile = root + "/" + index + "/" + type + Defaults.JsonFileExtension;
		return readFileFromClasspath(mappingFile);
	}

	/**
	 * Read a mapping in default classpath dir
	 * @param index index name
	 * @param type type name (.json will be appended)
	 */
	public static String readMapping(String index, String type) throws IOException {
		return readMapping(Defaults.ConfigDir, index, type);
	}
}
