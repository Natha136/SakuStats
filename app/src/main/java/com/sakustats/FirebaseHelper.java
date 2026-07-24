package com.sakustats;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {

    public static FirebaseAuth auth =
            FirebaseAuth.getInstance();

    public static FirebaseFirestore db =
            FirebaseFirestore.getInstance();
}