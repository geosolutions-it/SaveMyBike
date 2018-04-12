'''
Created on 11 apr 2018

@author: gnafu
'''
from flask import Flask, jsonify, request, g
from flask_restful import reqparse, abort, Api, Resource
import psycopg2

from flask.json import JSONEncoder
from datetime import date

class CustomJSONEncoder(JSONEncoder):

    def default(self, obj):
        try:
            if isinstance(obj, date):
                return obj.isoformat()
            iterable = iter(obj)
        except TypeError:
            pass
        else:
            return list(iterable)
        return JSONEncoder.default(self, obj)


app = Flask(__name__)
app.json_encoder = CustomJSONEncoder
api = Api(app)

TODOS = {
    'todo1': {'task': 'build an API'},
    'todo2': {'task': '?????'},
    'todo3': {'task': 'profit!'},
}


def abort_if_todo_doesnt_exist(todo_id):
    if todo_id not in TODOS:
        abort(404, message="Todo {} doesn't exist".format(todo_id))

parser = reqparse.RequestParser()
parser.add_argument('task')

searchParser= reqparse.RequestParser()
searchParser.add_argument('orderBy').add_argument('page').add_argument('per_page').add_argument('tagId')


def connect_db():
    return psycopg2.connect("dbname=savemybike user=savemybike password=savemybike")

def get_db():
    """Opens a new database connection if there is none yet for the
    current application context.
    """
    if not hasattr(g, 'pgsql_db'):
        g.pgsql_db = connect_db()
    return g.pgsql_db

@app.teardown_appcontext
def close_db(error):
    """Closes the database again at the end of the request."""
    if hasattr(g, 'pgsql_db'):
        g.pgsql_db.close()

# Todo
# shows a single todo item and lets you delete a todo item
class Todo(Resource):
    def get(self, todo_id):
        abort_if_todo_doesnt_exist(todo_id)
        return TODOS[todo_id]

    def delete(self, todo_id):
        abort_if_todo_doesnt_exist(todo_id)
        del TODOS[todo_id]
        return '', 204

    def put(self, todo_id):
        args = parser.parse_args()
        task = {'task': args['task']}
        TODOS[todo_id] = task
        return task, 201


# TodoList
# shows a list of all todos, and lets you POST to add new tasks
class TodoList(Resource):
    def get(self):
        return TODOS

    def post(self):
        args = parser.parse_args()
        todo_id = int(max(TODOS.keys()).lstrip('todo')) + 1
        todo_id = 'todo%i' % todo_id
        TODOS[todo_id] = {'task': args['task']}
        return TODOS[todo_id], 201

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
        
        return vehicle_id

    def delete(self, vehicle_id):
        
        del TODOS[vehicle_id]
        return '', 204

    def put(self, vehicle_id):
        args = parser.parse_args()
        task = {'task': args['task']}
        TODOS[vehicle_id] = task
        return task, 201

##
## Actually setup the Api resource routing here
##
api.add_resource(VehiclesList, '/vehicles')
api.add_resource(Vehicle, '/vehicles/<vehicle_id>')

if __name__ == '__main__':
    app.run(debug=True)