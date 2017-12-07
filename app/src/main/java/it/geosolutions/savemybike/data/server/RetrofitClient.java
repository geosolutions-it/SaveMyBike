package it.geosolutions.savemybike.data.server;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;

import java.io.IOException;
import java.util.Locale;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.data.Constants;
import it.geosolutions.savemybike.model.Configuration;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Robert Oehler on 07.12.17.
 *
 */

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";

    private final static String ENDPOINT = "https://nlzuoba0r2.execute-api.us-west-2.amazonaws.com/demo/";

    private Retrofit retrofit;
    private OkHttpClient client;

    private Context context;

    public RetrofitClient(final Context context) {
        this.context = context;
    }

    public void getRemoteConfig(@NonNull final GetConfigCallback callback){

        ClientConfiguration clientConfiguration = new ClientConfiguration();

        // Create a CognitoUserPool object to refer to your user pool
        CognitoUserPool userPool = new CognitoUserPool(context, Constants.AWS_POOL, Constants.AWS_CLIENT_ID_W_SECRET, Constants.AWS_CLIENT_SECRET, clientConfiguration, Regions.US_WEST_2);

        CognitoUser currentUser = userPool.getCurrentUser();

        // Callback handler for the sign-in process
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {

                // Get id token from CognitoUserSession.
                String idToken = userSession.getIdToken().getJWTToken();

                Log.i(TAG, "got token "+ idToken);

                Call<Configuration> call = getServices(idToken).getConfig();

                try {
                    Configuration configuration = call.execute().body();

                    callback.gotConfig(configuration);

                }catch (IOException e){
                    Log.e(TAG, "error executing getConfig", e);
                    callback.error("error executing getConfig");
                }
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                // The API needs user sign-in credentials to continue
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(Constants.AWS_USER, Constants.AWS_PASS, null);

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                // Allow the sign-in to continue
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {

                Log.e(TAG, "Multi-factor authentication is required;");
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

                Log.w(TAG, "authenticationChallenge;");
            }

            @Override
            public void onFailure(Exception exception) {
                // Sign-in failed, check exception for the cause
                Log.e(TAG, "Sign-in failed "+ exception.getMessage());
            }
        };

        // get the current session
        currentUser.getSession(authenticationHandler);
    }


    private Retrofit getRetrofit(final String token){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .client(getClient(token))
                    .baseUrl(ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }
        return retrofit;
    }

    private OkHttpClient getClient(final String token){

        if(client == null) {
            client  = new OkHttpClient.Builder()
                    .addInterceptor(new TokenInterceptor(token))
                    .addInterceptor(new LoggingInterceptor())
                    .build();
        }
        return client;
    }

    /**
     * used to authenticate
     */
    private static class TokenInterceptor implements Interceptor {

        private String token;

        TokenInterceptor(String token) {
            this.token = token;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder().header("Authorization", token).build();
            return chain.proceed(authenticatedRequest);
        }
    }

    /**
     * used to log communication
     */
    private static class LoggingInterceptor implements Interceptor {

        @Override public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.nanoTime();
            String requestLog = String.format(Locale.US, "Sending request %s on %s%n%s",request.url(), chain.connection(), request.headers());

            if(request.method().compareToIgnoreCase("post")==0){
                requestLog ="\n"+requestLog+"\n"+bodyToString(request);
            }
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "request" + "\n" + requestLog);
            }
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();

            String responseLog = String.format(Locale.US, "Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers());
            String bodyString = response.body().string();

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "response" + "\n" + responseLog + "\n" + bodyString);
            }

            return response.newBuilder()
                    .body(ResponseBody.create(response.body().contentType(), bodyString))
                    .build();
        }
    }

    private static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            Log.e(TAG,"error bodyToString", e);
            return "error";
        }
    }

    private SMBRemoteServices getServices(final String token){

        return getRetrofit(token).create(SMBRemoteServices.class);

    }

    interface SMBRemoteServices {

        @GET("config")
        Call<Configuration> getConfig();
    }


    public interface GetConfigCallback
    {
        void gotConfig(Configuration configuration);
        void error(String message);
    }
}
