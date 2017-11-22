import {Config, CognitoIdentityCredentials} from "aws-sdk";
import {
  CognitoUserPool,
  CognitoUserAttribute,
  CognitoUser,
  AuthenticationDetails
} from "amazon-cognito-identity-js";
import React from "react";
import ReactDOM from "react-dom";
import appConfig from "./config";

Config.region = appConfig.region;
Config.credentials = new CognitoIdentityCredentials({
  IdentityPoolId: appConfig.IdentityPoolId
});

const userPool = new CognitoUserPool({
  UserPoolId: appConfig.UserPoolId,
  ClientId: appConfig.ClientId,
});

class SignUpForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      email: '',
      password: '',
      username: ''
    };
  }

  handleEmailChange(e) {
    this.setState({email: e.target.value});
  }

  handleUsernameChange(e) {
    this.setState({username: e.target.value});
  }

  handlePasswordChange(e) {
    this.setState({password: e.target.value});
  }

  handleSubmitSignup(e) {
    e.preventDefault();
    const email = this.state.email.trim();
    const password = this.state.password.trim();
    const attributeList = [
      new CognitoUserAttribute({
        Name: 'email',
        Value: email,
      })
    ];
    userPool.signUp(email, password, attributeList, null, (err, result) => {
      if (err) {
        console.log(err);
        return;
      }
      console.log('user name is ' + result.user.getUsername());
      console.log('call result: ' + result);
    });
  }

  handleSubmitSignIn(e) {
    e.preventDefault();
    const username = this.state.username.trim();
    const password = this.state.password.trim();

    const user = new CognitoUser({ Username: username, Pool: userPool });
    const authenticationData = { Username: username, Password: password };
    const authenticationDetails = new AuthenticationDetails(authenticationData);
  
    user.authenticateUser(authenticationDetails, {
      onSuccess: result => {
        console.log('Result: '+ JSON.stringify(result));
        alert("Logged in");
      },
      onFailure: err => {
        console.log('Failure: '+ err);
        alert(err);
      }
    })
  }

  render() {
    return (
      <div>
        <form onSubmit={this.handleSubmitSignIn.bind(this)}>
          <input type="text"
                value={this.state.username}
                placeholder="User"
                onChange={this.handleUsernameChange.bind(this)}/>
          <input type="password"
                value={this.state.password}
                placeholder="Password"
                onChange={this.handlePasswordChange.bind(this)}/>
          <input type="submit"/>
        </form>
        <br/>
        <form onSubmit={this.handleSubmitSignup.bind(this)}>
          <input type="text"
                value={this.state.email}
                placeholder="Email"
                onChange={this.handleEmailChange.bind(this)}/>
          <input type="password"
                value={this.state.password}
                placeholder="Password"
                onChange={this.handlePasswordChange.bind(this)}/>
          <input type="submit"/>
        </form>
      </div>
    );
  }
}

ReactDOM.render(<SignUpForm />, document.getElementById('app'));

