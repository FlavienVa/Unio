import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment
} from "@firebase/rules-unit-testing"
import { readFile } from "fs/promises"
import { setDoc, doc, updateDoc, getDoc, getDocs, collection } from "firebase/firestore";
import { alice, aliceAssociation, aliceEvent, otherAssociation, otherUser, setupFirestore } from './firestore-mock-data.js';

(async () => {
  /** Initialize testing environment **/  
  const testEnv = await initializeTestEnvironment({
    projectId: "unio-1b8ee",
    firestore: {
      rules: await readFile("firestore.rules", "utf8"),
      host: "localhost",
      port: 8080
    },
  });
  
  /** Load data **/
  await setupFirestore(testEnv);

  /** Run tests **/
  console.log("Running tests...");

  try {
    await runTests(testEnv);
  } catch(e) {
    console.error("\x1b[31mTests failed, cleaning up.\x1b[0m");
    await testEnv.clearFirestore();

    throw e;
  }

  console.log("\x1b[32mAll tests passed successfully!\x1b[0m");
  await testEnv.clearFirestore();
  await testEnv.cleanup();

})();

async function runTests(testEnv) {
  const aliceAuth = testEnv.authenticatedContext(alice.uid, {
    email_verified: true,
    email: alice.email
  });  
  const aliceDb = aliceAuth.firestore();

  /** Reading and writing to users **/
  await assertSucceeds(setDoc(doc(aliceDb, `/users/${alice.uid}`), alice));
  await assertSucceeds(getDoc(doc(aliceDb, `/users/${alice.uid}`)));
  await assertFails(setDoc(doc(aliceDb, `/users/${alice.uid}`), { ...alice, uid: "other" }));
  await assertFails(setDoc(doc(aliceDb, `/users/${alice.uid}`), { ...alice, email: "other" }));
  await assertFails(updateDoc(doc(aliceDb, `/users/${otherUser.uid}`), alice));
  await assertSucceeds(getDoc(doc(aliceDb, `/users/${otherUser.uid}`)));
  await assertFails(getDocs(collection(aliceDb, `/users`)));

  /** Reading and writing to associations **/
  await assertSucceeds(setDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), aliceAssociation));
  await assertFails(setDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), { ...aliceAssociation, uid: "other" }));
  await assertSucceeds(getDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`)));
  await assertFails(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), aliceAssociation));
  await assertSucceeds(updateDoc(doc(aliceDb, `/associations/${aliceAssociation.uid}`), { ...aliceAssociation, name: "New name" }));
  await assertSucceeds(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), { ...otherAssociation, followersCount: 1 }));
  await assertSucceeds(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), { ...otherAssociation, followersCount: 0 }));
  await assertFails(updateDoc(doc(aliceDb, `/associations/${otherAssociation.uid}`), { ...otherAssociation, followersCount: 1000 }));
  await assertSucceeds(getDocs(collection(aliceDb, `/associations`)));

  /** Deny all unauthenticated requests **/
  const unAuthenticated = testEnv.unauthenticatedContext();
  const unAuthenticatedDb = unAuthenticated.firestore();
  await assertFails(getDoc(doc(unAuthenticatedDb, `/users/${alice.uid}`)));
  await assertFails(getDocs(collection(unAuthenticatedDb, `/users`)));
  await assertFails(getDoc(doc(unAuthenticatedDb, `/events/${aliceEvent.uid}`)));
  await assertFails(getDocs(collection(unAuthenticatedDb, `/events`)));
  await assertFails(getDoc(doc(unAuthenticatedDb, `/associations/${alice.uid}`)));
  await assertFails(getDocs(collection(unAuthenticatedDb, `/associations`)));

  /** Deny all authenticated but not email verified requests **/
  const unverified = testEnv.authenticatedContext(alice.uid, {
    email_verified: false,
    email: alice.email
  });
  const unVerifiedDb = unverified.firestore();
  await assertFails(getDoc(doc(unVerifiedDb, `/users/${alice.uid}`)));
  await assertFails(getDocs(collection(unVerifiedDb, `/users`)));
  await assertFails(getDoc(doc(unVerifiedDb, `/events/${aliceEvent.uid}`)));
  await assertFails(getDocs(collection(unVerifiedDb, `/events`)));
  await assertFails(getDoc(doc(unVerifiedDb, `/associations/${alice.uid}`)));
  await assertFails(getDocs(collection(unVerifiedDb, `/associations`)));

}