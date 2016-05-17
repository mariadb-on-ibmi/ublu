# audJournal.sh ... read entries from audit journal 
# autogenerated Wed Mar 09 19:26:40 MST 2016 by jax using command:
# gensh -to audJournal.sh -path /opt/ublu/ublu.jar -optr s SERVER @server ${ Server }$ -optr u USER @user ${ User }$ -optr p PASSWORD @password ${ Password }$ -optr n NUMENTRIES @numentries ${ number of journal entries to read }$ -optr f FILENAME @filename ${ filename to write journal to }$ ${ audJournal.sh ... read entries from audit journal }$ /opt/api-java/examples/audJournal.ublu ${ audJournal ( @server @user @password @numentries @filename ) }$

# Usage message
function usage { 
echo "audJournal.sh ... read entries from audit journal "
echo "This shell script was autogenerated Wed Mar 09 19:26:40 MST 2016 by jax."
echo "Usage: $0 [silent] -h -s SERVER -u USER -p PASSWORD -n NUMENTRIES -f FILENAME "
echo "	where"
echo "	-h		display this help message and exit 0"
echo "	-s SERVER	Server  (required option)"
echo "	-u USER	User  (required option)"
echo "	-p PASSWORD	Password  (required option)"
echo "	-n NUMENTRIES	number of journal entries to read  (required option)"
echo "	-f FILENAME	filename to write journal to  (required option)"
echo "---"
echo "If the keyword 'silent' appears ahead of all options, then included files will not echo and prompting is suppressed."
echo "Exit code is the result of execution, or 0 for -h or 2 if there is an error in processing options"
}

#Test if user wants silent includes
if [ "$1" == "silent" ]
then
	SILENT="-silent "
	shift
else
	SILENT=""
fi

# Process options
while getopts s:u:p:n:f:h the_opt
do
	case "$the_opt" in
		s)	SERVER="$OPTARG";;
		u)	USER="$OPTARG";;
		p)	PASSWORD="$OPTARG";;
		n)	NUMENTRIES="$OPTARG";;
		f)	FILENAME="$OPTARG";;
		h)	usage;exit 0;;
		[?])	usage;exit 2;;

	esac
done
shift `expr ${OPTIND} - 1`
if [ $# -ne 0 ]
then
	echo "Superfluous argument(s) $*"
	usage
	exit 2
fi

# Translate options to tuple assignments
if [ "${SERVER}" != "" ]
then
	gensh_runtime_opts="${gensh_runtime_opts}string -to @server -trim \${ ${SERVER} }$ "
else
	echo "Option -s SERVER is a required option but is not present."
	usage
	exit 2
fi
if [ "${USER}" != "" ]
then
	gensh_runtime_opts="${gensh_runtime_opts}string -to @user -trim \${ ${USER} }$ "
else
	echo "Option -u USER is a required option but is not present."
	usage
	exit 2
fi
if [ "${PASSWORD}" != "" ]
then
	gensh_runtime_opts="${gensh_runtime_opts}string -to @password -trim \${ ${PASSWORD} }$ "
else
	echo "Option -p PASSWORD is a required option but is not present."
	usage
	exit 2
fi
if [ "${NUMENTRIES}" != "" ]
then
	gensh_runtime_opts="${gensh_runtime_opts}string -to @numentries -trim \${ ${NUMENTRIES} }$ "
else
	echo "Option -n NUMENTRIES is a required option but is not present."
	usage
	exit 2
fi
if [ "${FILENAME}" != "" ]
then
	gensh_runtime_opts="${gensh_runtime_opts}string -to @filename -trim \${ ${FILENAME} }$ "
else
	echo "Option -f FILENAME is a required option but is not present."
	usage
	exit 2
fi

# Invocation
java -jar /opt/ublu/ublu.jar ${gensh_runtime_opts} include ${SILENT}/opt/api-java/examples/audJournal.ublu audJournal \( @server @user @password @numentries @filename \) 
exit $?