'''
Created on 13 apr 2018

@author: gnafu
'''
from flask import Flask, g
from flask_restful import  Api
from flask.json import JSONEncoder
from Vehicles import Vehicle, VehiclesList
from Users import User, UsersList
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

## app.register_blueprint(vehicles_api)

@app.teardown_appcontext
def close_db(error):
    """Closes the database again at the end of the request."""
    if hasattr(g, 'pgsql_db'):
        g.pgsql_db.close()


api = Api(app)

api.add_resource(VehiclesList, '/vehicles')
api.add_resource(Vehicle, '/vehicles/<vehicle_id>')

api.add_resource(UsersList, '/users')
api.add_resource(User, '/users/<user_id>')

if __name__ == '__main__':
    app.run(debug=True)