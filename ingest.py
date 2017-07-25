#! /usr/bin/env python

import boto3 as boto
import sys
import time
import datetime
import os
import tempfile
import gzip
import jinja2 as jinja
import json
import hashlib
import sys

"""
This file updates the file catalogue of a THREDDS/OpenDAP
Docker container, which allows lazy access of s3 data files.
Bucket names are provided as arguments to the file call.

AWS credentials must have access to S3.
"""

def write_inventory_names_file(bucket_name, region='eu-west-2'):
    """
    AWS splits the filenames over a bunch of UID names files.
    For a given dates, it defines which files are relevant in
    a different files (a sort of inventory of the inventory files).
    This gets the inventory inventory...

    """
    s3 = boto.resource('s3',
                        aws_secret_access_key=os.environ['AWS_SECRET_ACCESS_KEY'],
                        aws_access_key_id=os.environ['AWS_ACCESS_KEY_ID'])
    bucket = s3.Bucket(bucket_name)

    now = datetime.datetime.now()-datetime.timedelta(days=1) #incase todays hasn't happened yet
    inventory_name_prefix = "{:}-{:02}-{:02}T".format(now.year, now.month, now.day)
    print("Getting manifest for " + inventory_name_prefix)
    filt_objects = bucket.objects.filter(Prefix=bucket_name+'/'+bucket_name+'/'+inventory_name_prefix)
    filt_object_keys = [filt_object.key for filt_object in filt_objects]
    [manifest_key] = [filt_object_key for filt_object_key in filt_object_keys if filt_object_key.endswith('json')]

    with tempfile.NamedTemporaryFile(suffix='.json', delete=False) as f:
        bucket.download_file(manifest_key, f.name)
        with open(f.name) as g:
            mf = json.load(g)

    local_manifests = []
    for this_file in mf['files']:
        with tempfile.NamedTemporaryFile(suffix='.csv.gz', delete=False) as f:
            bucket.download_file(this_file['key'], f.name)
        with open(f.name, 'rb') as g:
            assert hashlib.md5(g.read()).hexdigest() == this_file['MD5checksum']
        local_manifests.append(f.name)

    manifest_fname = bucket_name+'.txt'
    with open(manifest_fname, 'w') as fout:
        for local_manifest in local_manifests:
            with gzip.open(local_manifest) as fin:
                lines = fin.readlines()
                for line in lines:
                    file_name = line.decode().split(',')[1].replace('\"', '')
                    fout.write(file_name+"\n")

    return manifest_fname


def update_threeds_catalogue(local_manifest_fname, bucket_name, catalogue_file_template, catalogue_file):
    loader = jinja.FileSystemLoader('/usr/local/src/')
    env = jinja.Environment(loader=loader)
    template = env.get_template(catalogue_file_template)
    
    with open(local_manifest_fname, 'r') as fin:
        obj_names = fin.read().splitlines()
        output_from_parsed_template = template.render(obj_names=obj_names[:10],
                                                      bucket_name=bucket_name)
    with open(catalogue_file, 'w') as fout:
        fout.write(output_from_parsed_template)
    

if __name__=='__main__':
    print("Getting inventory from s3 for...")

    catalog_file = sys.argv[1]
    bucket_catalogue_parts = [('mogreps-uk', 'catalog.jinja', catalog_file)]
    
    for bucket_name, template_catalogue_file, catalogue_file in bucket_catalogue_parts:
        print(bucket_name)
        manifest_fname = write_inventory_names_file(bucket_name)
        update_threeds_catalogue(manifest_fname, bucket_name, template_catalogue_file, catalogue_file)
    
    print("Thredds catalogue "+catalogue_file+" updated")


