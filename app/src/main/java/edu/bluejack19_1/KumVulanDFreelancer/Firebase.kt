package edu.bluejack19_1.KumVulanDFreelancer

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

fun firebaseAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
}

fun firebaseDatabase(): FirebaseFirestore {
    return FirebaseFirestore.getInstance()
}
fun firebaseStorage(): FirebaseStorage {
    return FirebaseStorage.getInstance()
}

fun firebaseStorageReference(): StorageReference {
    return firebaseStorage().reference
}

fun firebaseUser(): FirebaseUser? {
    return firebaseAuth().currentUser
}