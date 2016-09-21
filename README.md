# emc-cf-postgresql-service-broker-bosh-release

A cloudfoundry service broker for postgresql.

Support Service Broker API `v2.3`

Java version: 1.8.45

Jetty version: 8.1.0.RC5

## Deployment

### Editing Manifest File

We have a [sample deployment manifest file](https://github.com/emc-cloudfoundry/emc-cf-postgresql-service-broker-bosh-release/blob/master/pg_broker.yml), you can simply edit it refer to your environment.

### Creating & Uploading the Bosh Release

```sh
$ bosh target BOSH_HOST
$ git clone https://github.com/emc-cloudfoundry/emc-cf-postgresql-service-broker-bosh-release.git
$ cd emc-cf-postgresql-service-broker-bosh-release
$ bosh create release --force
$ bosh create release --force --final
$ bosh upload release
```

### Deploying

Using the previous created deployment manifest, now we can deploy it:

```sh
$ bosh deployment path/to/manifest.yml
$ bosh -n deploy
```

## Setup Deployment Environment

Postgresql service broker is built by Gradle, we use Gradle wrapper so you would not be concerned about installing gradle in your workstation.

Here we assume your workstation is a windows machine. After downloading the source code from Perforce, open command line tool and cd to the source code path, run the command:

```bat
C:\...\emc-cf-postgresql-service-broker-bosh-release> gradlew.bat build
```

The command will download the gradle from internet and dependencies from EMC Artifactory, then build the project.

Now you might want to use IDE to open project, here is a command if you use intelliJ IDEA.

```bat
C:\...\emc-cf-postgresql-service-broker-bosh-release> gradlew.bat idea
```

Or you’re using eclipse? Try this.

```bat
C:\...\emc-cf-postgresql-service-broker-bosh-release> gradlew.bat eclipse
```

The command can generate the essential files for these two IDEs, and then you can open the project with your lovely one.
