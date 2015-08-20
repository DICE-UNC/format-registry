# Format Registry

This is a simple web frontend for serving up file format data.

## Dependencies

The Format Registry requires Maven to build and run. Additionally, there are some artifacts not available in maven central. These have been provided in the `deps/` subdirectory. The `README` in that folder contains instructions for isntalling them (and is itself an executable script).

JDK 7+ is required to build the Format Registry.

The Format Registry relies on PostgreSQL for storage and requires a properly configured database. Scripts to assist with this setup can be found in `src/main/resources/*.sql`.

## Running

Compile the system with

    mvn clean install

Run the system with

    mvn jetty:run

After the first run, you will need to populate the `~/.format_reg/irods.properties` file with values appropriate to your system.


## Running Automatically

You can automatically launch this in the background by adding a line like

    (cd /var/www/formatreg; su -c "/usr/bin/mvn jetty:run" formatreg >> /var/log/formatreg/output.log 2>&1)

to the end of `/etc/rc.local`.
For all I know this is an Ubuntu-specific solution, and obviously depends on the code being located at `/var/www/formatreg`, the log directory `/var/log/formatreg` existing, and both having appropriate permissions granted to a `formatreg` OS user.
This may be better handled by a real system script, but this works for now.
