'''
Created on 13 apr 2018

@author: gnafu
'''

from flask import jsonify, request
from flask_restful import reqparse, Resource

from Database import get_db

searchParser= reqparse.RequestParser()
searchParser.add_argument('orderBy').add_argument('page').add_argument('per_page').add_argument('tagId')

# VehiclesList
# shows a list of all vehicles, and lets you POST to add new vehicles
class VehiclesList(Resource):
    def get(self):
        args = searchParser.parse_args()
        print(args)
        print(request.args)
        conn = get_db()
        cur = conn.cursor()
        cur.execute("SELECT * FROM vehicles order by id limit 50;")
        # row = cur.fetchone()
        rows = cur.fetchall()
        if rows == None:
            print("There are no results for this query")
            rows = []
        
        columns = [desc[0] for desc in cur.description]
        result = []
        for row in rows:
            row = dict(zip(columns, row))
            result.append(row)

        conn.commit()
        cur.close()
        return jsonify(result)

    def post(self):
        content = request.json
        print(content)
        
        _type = content.get('type', 1)
        name = content.get('name', None)
        status = content.get('status', 0)
        lastposition  = content.get('lastposition', None)
        image  = content.get('image', None)
        owner  = content.get('owner', None)
        
        
        conn = get_db()
        cur = conn.cursor()
        
        SQL = "INSERT INTO vehicles (type, name, status, lastposition, image, owner) VALUES (%s, %s, %s, %s, %s, %s) RETURNING id;" 
        data = (_type, name, status, lastposition, image, owner )
        cur.execute(SQL, data) 
        id_of_new_row = cur.fetchone()[0]        
        
        conn.commit()
        cur.close()
        
        return id_of_new_row, 201

# Vehicle
# shows a single Vehicle item and lets you delete a Vehicle item
class Vehicle(Resource):
    def get(self, vehicle_id):
        
        try:
            int(vehicle_id)
        except ValueError: 
            return None # the input is not an integer
        
        conn = get_db()
        cur = conn.cursor()
        SQL = "SELECT * FROM vehicles where id = %s limit 1;" 
        data = (vehicle_id,) # keep the comma to make it a tuple
        cur.execute(SQL, data) 
        rows = cur.fetchall()
        if rows == None:
            print("There are no results for this query")
            rows = []
        
        columns = [desc[0] for desc in cur.description]
        result = []
        for row in rows:
            row = dict(zip(columns, row))
            result.append(row)

        conn.commit()
        cur.close()
        return jsonify(result)

    def delete(self, vehicle_id):
        conn = get_db()
        cur = conn.cursor()
        
        SQL = "DELETE FROM vehicles WHERE id = %s;" 
        data = (vehicle_id )
        cur.execute(SQL, data) 
        
        conn.commit()
        cur.close()
        return '', 204

    def post(self, vehicle_id):
        content = request.json #: :type content: dict
        print(content)
        
        if content is None: return None, 304
        
        _type = content.get('type', 1)
        name = content.get('name', None)
        status = content.get('status', 0)
        lastposition  = content.get('lastposition', None)
        image  = content.get('image', None)
        owner  = content.get('owner', None) #: :type owner: tuple
        
        
        conn = get_db()
        cur = conn.cursor()
        
        inputslist = []
        SQL = "UPDATE vehicles SET lastupdate = now()" 
        if 'type' in content :
            SQL += ', type = %s'
            inputslist.append(_type)
        if 'name' in content :
            SQL += ', name = %s'
            inputslist.append(name)
        if 'status' in content :
            SQL += ', status = %s'
            inputslist.append(status)
        if 'lastposition' in content :
            SQL += ', lastposition = %s'
            inputslist.append(lastposition)
        if 'image' in content :
            SQL += ', image = %s'
            inputslist.append(image)
        if 'owner' in content :
            SQL += ', owner = %s'
            inputslist.append(owner)
        
        SQL += " where id = %s RETURNING id;"
        inputslist.append(vehicle_id)
        
        data = tuple(inputslist)
        cur.execute(SQL, data) 
        id_of_new_row = cur.fetchone()[0]        
        
        conn.commit()
        cur.close()
        
        return id_of_new_row, 201
