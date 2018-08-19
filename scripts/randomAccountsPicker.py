#!/usr/bin/env python2

import json
import string
from random import *

max_accounts = 45

min_char = 8
max_char = 12
allchar = string.ascii_letters + string.punctuation + string.digits

def randomPass():
    return "".join(choice(allchar) for x in range(randint(min_char, max_char)))
    print "This is your password : ", password

all = json.load(open('/tmp/lala.json'))
filtered = [ i for i in all if i['modification date'] != "" and i["creation date"] != "" ]

for acc in filtered:
    acc['password'] = randomPass()

json.dump(filtered[:max_accounts], open('/tmp/random.json', 'w'))