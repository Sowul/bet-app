package info.czekanski.bet.repository

import com.google.firebase.firestore.FirebaseFirestore
import durdinapps.rxfirebase2.RxFirestore
import info.czekanski.bet.misc.applySchedulers
import info.czekanski.bet.network.firebase.model.FirebaseBet
import info.czekanski.bet.user.UserProvider
import io.reactivex.Flowable

class BetRepository(
        val firestore: FirebaseFirestore,
        val userProvider: UserProvider
) {

    fun getBets(): Flowable<List<FirebaseBet>> {
        return RxFirestore.observeQueryRef(firestore.collection("bets").whereEqualTo("users.${userProvider.userId!!}", true))
                .map {
                    it.documents
                            .filterNotNull()
                            .map { it.toObject(FirebaseBet::class.java)!!.copy(id = it.id) }
                }
                .applySchedulers()
    }

    fun observeBet(betId: String): Flowable<FirebaseBet> {
        return RxFirestore.observeDocumentRef(firestore.collection("bets").document(betId))
                .filter { it.exists() }
                .map { it.toObject(FirebaseBet::class.java)!!.copy(id = it.id) }
                .applySchedulers()
    }

    companion object {
        val instance by lazy {
            BetRepository(FirebaseFirestore.getInstance(), UserProvider.instance)
        }
    }
}