/*
 * Copyright 2015 MiLaboratory.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.milaboratory.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.primitivio.annotations.Serializable;

import java.io.InputStream;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
@Serializable(asJson = true)
public class VersionInfo {
    final String version, revision, name, branch, host;
    final boolean production;
    final Date timestamp;

    public VersionInfo(@JsonProperty("version") String version,
                       @JsonProperty("revision") String revision,
                       @JsonProperty("name") String name,
                       @JsonProperty("branch") String branch,
                       @JsonProperty("host") String host,
                       @JsonProperty("production") boolean production,
                       @JsonProperty("timestamp") Date timestamp) {
        this.version = version;
        this.revision = revision;
        this.name = name;
        this.branch = branch;
        this.host = host;
        this.production = production;
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public String getRevision() {
        return revision;
    }

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }

    public String getHost() {
        return host;
    }

    public boolean isProductionBuild() {
        return production;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(revision, that.revision) &&
                Objects.equals(name, that.name) &&
                Objects.equals(branch, that.branch) &&
                Objects.equals(host, that.host) &&
                production == that.production &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, revision, name, branch, host, production, timestamp);
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "version='" + version + '\'' +
                ", revision='" + revision + '\'' +
                ", name='" + name + '\'' +
                ", branch='" + branch + '\'' +
                ", host='" + host + '\'' +
                ", production=" + production +
                ", timestamp=" + timestamp +
                '}';
    }

    public static VersionInfo getVersionInfoForArtifact(String artifactId) {
        return getVersionInfo(className(artifactId), "/" + artifactId + "-build.properties");
    }

    static String longest(String s1, String s2) {
        if (s1 == null)
            return s2;
        else if (s2 == null)
            return s1;
        else if (s1.length() > s2.length())
            return s1;
        else
            return s2;
    }

    static String className(String resourceName) {
        switch (resourceName) {
            case "milib":
                return "com.milaboratory.util.VersionInfo";
            case "mixcr":
                return "com.milaboratory.mixcr.util.MiXCRVersionInfo";
            case "repseqio":
                return "io.repseq.util.RepseqIOVersionInfo";
            default:
                return resourceName;
        }
    }

    static VersionInfo getVersionInfo(String className, String resourceName) {
        Properties properties = new Properties();
        try (InputStream is = Class.forName(className).getResourceAsStream(resourceName)) {
            properties.load(is);
        } catch (Exception ex) {
            return null;
        }
        return new VersionInfo(properties.getProperty("version"),
                properties.getProperty("revision"),
                properties.getProperty("name"),
                properties.getProperty("branch"),
                properties.getProperty("host"),
                "true".equals(properties.getProperty("production")),
                new Date(Long.parseLong(properties.getProperty("timestamp"))));
    }
}
