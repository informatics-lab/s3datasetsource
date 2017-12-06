# S3 Dataset Source Plugin for [Unidata's THREDDS Project][1].

Provides a simple implementation of the [thredds DatasetSource interface][2].  
Allows datasets to reside in, and be accessed directly from S3 via the [THREDDS Data Server (TDS)][3].

## Configuration
Follow the documentation on the [thredds DatasetSource plugin page][2].  
`catalog.xml` - thredds catalog configuration.  
`threddsConfig.xml` - thredds  main configuration file.  
`docker-compose.yml` - docker compose file to start thredds TDS with supplied configuration.  

## Building
`$ mvn install` - build the plugin.  
Built artifact can be found in the target directory:  
`<project root>/target/s3datasetsource-1.0-SNAPSHOT-jar-with-dependencies.jar`

Note that there is no need to build this project if you are using the Docker image.

## Running
Store your AWS credentials in a file called `aws.env` in the root directory. You have to have a (free) AWS account to access this data.

`$ docker-compose up` - builds and starts the thredds TDS with the supplied configuration.

The data can then be seen at `http://localhost/thredds/catalogue.html

### Credit  
[@jamesmcclain](https://github.com/jamesmcclain) original author of [S3RandomAccessFile][4].

[1]: https://github.com/Unidata/thredds
[2]: http://www.unidata.ucar.edu/software/thredds/current/tds/reference/DatasetSource.html
[3]: http://www.unidata.ucar.edu/software/thredds/current/tds/
[4]: https://github.com/Unidata/thredds/pull/832/files#diff-fd2b60e4477724acec18731154b8db0a

