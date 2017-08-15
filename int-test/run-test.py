import glob
import sys
import os
import subprocess
import argparse
import re


parser = argparse.ArgumentParser()
parser.add_argument("--filter", help="RegEx: Only tests that match will run")
args = parser.parse_args()
print(args)
test_filter = None
if(args.filter):
    test_filter = re.compile(args.filter)


FILTER = 'TEST_EVENT:'
# path = sys.argv[1] if len(sys.argv[1]) > 0 else '*'
os.chdir(os.path.dirname(__file__))
containers = os.listdir('containers')
os.chdir('containers')
for container in containers:
    os.chdir(container)
    subprocess.run(["docker","build", "-t", "iris","."])
    os.chdir('..')

os.chdir('..')

tests = os.listdir('tests')
os.chdir('tests')
for test in tests:
    if test_filter and not test_filter.match(test):
        continue
    os.chdir(test)
    print('Start test %s'% test)
    print(os.path.abspath(os.curdir))
    (status, output) = subprocess.getstatusoutput("docker-compose up --build --exit-code-from test")
    events = [l[l.find(FILTER) + len(FILTER):] for l in output.split('\n') if l.find(FILTER) >=0]
    print('stats: %s', status)
    if(status != 0):
        print("FAIL!!!!!")
        print(output)
    else:
        print('Test %s passed' % test)
        print('\n'.join(events))
    os.chdir('..')