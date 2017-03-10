package com.citaurus.gmspps;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;

/**
 * Created by rzwisler on 10.01.2017.
 */

public class GoogleLoginStackOverflow {
    private static final String TAG = GoogleLoginStackOverflow.class.getName();
    private static final String SERVER_CLIENT_ID = "414999757757-meg30nbsf899quqhhubvarf2cjf3guk5.apps.googleusercontent.com"; // für Backend

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public GoogleLoginStackOverflow(Context appContext) {
        this.mContext = appContext;
        createGoogleClient();
        silentLogin();
    }

    /**
     * Performs a silent sign in and fetch a token.
     *
     * @param appContext Application context
     */
    public static void silentLogin(Context appContext) {
        GoogleLoginStackOverflow googleLoginIdToken = new GoogleLoginStackOverflow(appContext);
        googleLoginIdToken.silentLogin();
    }

    private void createGoogleClient() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        System.out.println("onConnectionFailed  = " + connectionResult);
                        onSilentLoginFinished(null);
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        System.out.println("onConnected bundle = " + bundle);
                        onSilentLoginFinished(null);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        System.out.println("onConnectionSuspended i = " + i);
                        onSilentLoginFinished(null);
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void silentLogin() {

        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult != null) {
            if (pendingResult.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, " ----------------  CACHED SIGN-IN ------------");
                System.out.println("pendingResult is done = ");
                GoogleSignInResult signInResult = pendingResult.get();
                onSilentLoginFinished(signInResult);
            } else {
                System.out.println("Setting result callback");
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        System.out.println("googleSignInResult = " + googleSignInResult);
                        onSilentLoginFinished(googleSignInResult);
                    }
                });
            }
        } else {
            onSilentLoginFinished(null);
        }
    }

    private void onSilentLoginFinished(GoogleSignInResult signInResult) {
        System.out.println("GoogleLoginIdToken.onSilentLoginFinished");
        if (signInResult != null) {
            GoogleSignInAccount signInAccount = signInResult.getSignInAccount();
            if (signInAccount != null) {
                String emailAddress = signInAccount.getEmail();
                String token = signInAccount.getIdToken();
                System.out.println("token = " + token);
                System.out.println("emailAddress = " + emailAddress);
            }
        }
    }
}
