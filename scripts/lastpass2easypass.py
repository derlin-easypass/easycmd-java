#!/usr/bin/env python

import time
import re
import json
import csv
from collections import OrderedDict

nameRegex = re.compile("https?:\/\/([^\/]+)\/?.*")
emailRegex = re.compile("^[^@]+@[^@\.]+\.[a-zA-Z]{2,}$")
now = time.strftime('%Y-%m-%d %H:%M:%S')

class Record:

    def __init__(self, csvRow):
        self.raw = ", ".join(csvRow)
        self.url, self.username, self.password, self.extra, self.name, self.grouping, self.fav = csvRow

    def easyName(self):
        if self.name is not '': return self.name 

        m = nameRegex.match(self.url)
        if m is not None: return m.group(1)
        raise ValueError('Can\'t figure out a name for %s does not have a name.' % self.raw)

    def isUsingPseudo(self):
        return emailRegex.match(self.username) is None

    def easyDict(self):
        d = OrderedDict()
        d['name'] = self.easyName()
        d['pseudo'] = ['', self.username][self.isUsingPseudo()]
        d['email'] = [self.username, ''][self.isUsingPseudo()]
        d['password'] = self.password
        d['notes'] = self.extra
        d['modification_date'] = now
        d['creation_date'] = ''
        return d


if __name__ == "__main__":
    with open('lastpass.csv') as f:
        reader = csv.reader(f)
        records = [Record(r) for r in reader]
        converted = [r.easyDict() for r in records[1:]]

        with open('export.json', 'w') as f:
            json.dump(converted, f, indent=2)
            print("done.")
