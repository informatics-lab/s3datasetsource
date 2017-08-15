from datetime import datetime
import iris


start = datetime.now()
iris.load_cube('http://thredds:8080/thredds/dodsC/s3/mogreps-g/prods_op_mogreps-g_20160818_00_09_087.nc')
end = datetime.now()




print('TEST_EVENT:Iris load in Done in %ss'% (end - start))