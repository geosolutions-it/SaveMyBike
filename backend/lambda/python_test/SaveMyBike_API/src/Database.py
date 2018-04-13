'''
Created on 13 apr 2018

@author: gnafu
'''

from flask import  g
import psycopg2


def connect_db():
    return psycopg2.connect("dbname=savemybike user=savemybike password=savemybike")

def get_db():
    """Opens a new database connection if there is none yet for the
    current application context.
    """
    if not hasattr(g, 'pgsql_db'):
        g.pgsql_db = connect_db()
    return g.pgsql_db
