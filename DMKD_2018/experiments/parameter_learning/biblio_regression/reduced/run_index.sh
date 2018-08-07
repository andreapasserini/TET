for t in eindex gindex hindex i10index
do
    ./run_biblio_regression.sh AMiner *dbuser* *dbpassword* tet-def logs/$t\_MEMD.log
done
