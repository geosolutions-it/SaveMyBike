'''
Created on 13 apr 2018

@author: gnafu
'''

from flask import jsonify, request
from flask_restful import reqparse, Resource

from Database import get_db
from Utility import limit_int

from geojson import Point

# https://developer.github.com/v3/guides/traversing-with-pagination/
# https://developer.wordpress.org/rest-api/using-the-rest-api/pagination/
searchParser= reqparse.RequestParser()
searchParser.add_argument('orderBy').add_argument('page').add_argument('per_page').add_argument('tagId')

# VehiclesList
# shows a list of all vehicles, and lets you POST to add new vehicles
class VehiclesList(Resource):
    def get(self):
        
        args = searchParser.parse_args()

        per_page = 50;
        offset = 0;
        tagId = None
        
        if args['per_page'] is not None:
            try:
                per_page=limit_int(int(args['per_page']), 0, 100)
            except ValueError: 
                pass
        
        if args['page'] is not None:
            try:
                offset=limit_int(int(args['page']) * per_page, 0)
            except ValueError: 
                pass
            
        if args['tagId'] is not None:
            try:
                tagId=limit_int(int(args['tagId']), 0)
            except ValueError: 
                pass
            
        
        if tagId is not None:
            SQL="SELECT v.* FROM vehicles as v JOIN tags as t ON v.id = t.vehicle_id where t.epc = %s;"
            data = (tagId,)
        else :            
            SQL="SELECT * FROM vehicles order by id limit %s offset %s;"
            data = (per_page, offset)
            
        conn = get_db()
        cur = conn.cursor()
        cur.execute(SQL, data)
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


# TagsList
# shows a list of all the tags associated with a specific Vehicle, and lets you POST to add new vehicles
class TagsList(Resource):
    def get(self, vehicle_id):
        
        try:
            int(vehicle_id)
        except ValueError: 
            return None # the input is not an integer
        
        args = searchParser.parse_args()

        conn = get_db()
        cur = conn.cursor()
        SQL = "SELECT epc FROM tags where vehicle_id = %s order by epc limit 50;" 
        data = (vehicle_id,) # keep the comma to make it a tuple
        cur.execute(SQL, data) 
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

    def post(self, vehicle_id):
        content = request.json
        print(content)
        
        _id = content.get('epc', 1)
                
        conn = get_db()
        cur = conn.cursor()
        
        SQL = "INSERT INTO tags (epc, vehicle_id) VALUES (%s, %s) RETURNING epc;" 
        data = (_id, vehicle_id )
        id_of_new_row = None
        error_message = ""        
        
        try:
            cur.execute(SQL, data) 
        except Exception as e:
            print(e)
            if hasattr(e, 'diag') and hasattr(e.diag, 'message_detail') :
                error_message = e.diag.message_detail
            else :
                error_message = "Database error" 
            conn.rollback()
        else:
            conn.commit()
            id_of_new_row = cur.fetchone()[0]        
        
        cur.close()
        
        # TODO : 409 Conflict if tagId already exists
        if id_of_new_row is None : return {"Error" : error_message}, 404
        
        return id_of_new_row, 201
