package info.czekanski.bet.domain.home

import android.arch.lifecycle.*
import android.util.Log
import info.czekanski.bet.domain.home.cells.*
import info.czekanski.bet.misc.Cell
import info.czekanski.bet.model.MatchState
import info.czekanski.bet.repository.*
import info.czekanski.bet.user.UserProvider
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.*

class HomeViewModel : ViewModel() {
    private val betRepository by lazy { BetRepository.instance }
    private val matchesRepository by lazy { MatchRepository.instance }
    private val userProvider by lazy { UserProvider.instance }
    private val preferencesProvider by lazy { PreferencesProvider._instance!!} // TODO: Kill it with fire!!!!!
    private val liveCells = MutableLiveData<List<Cell>>()
    private var subscription: Disposable? = null

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
        subscription = null
    }

    private fun loadData() {
        val betsFlowable = betRepository.getBets()
        val matchesFlowable = matchesRepository.getMatches()
        val userNameFlowable = userProvider.loadNick().toFlowable()

        subscription = Flowables.combineLatest(betsFlowable, matchesFlowable, userNameFlowable, { b, m, _ -> Pair(b, m) })
                .doOnSubscribe {
                    liveCells.value = listOf(LoaderCell())
                }
                .subscribeBy(onNext = {
                    val (bets, matches) = it
                    val cells = mutableListOf<Cell>(WelcomeCell(userProvider.nick, preferencesProvider.runCount < 3))

                    if (bets.isNotEmpty()) {
                        val betsWithMatches = bets.map { bet ->
                            bet.copy(match = matches.find { match ->
                                match.id == bet.matchId
                            })
                        }.sortedBy { it.match?.date }
                        cells += HeaderCell("Twoje typy")
                        cells += betsWithMatches.map { BetCell(it) }
                    }

                    if (matches.isNotEmpty()) {
                        cells += HeaderCell("Najbliższe mecze")
                        cells += matches
                                .filter { it.state != MatchState.AFTER }
                                .take(4)
                                .map { MatchCell(it) }
                    }

                    liveCells.value = cells
                }, onError = {
                    Log.e("HomeFragment", "LoadMatchesAndBets", it)
                })
    }

    fun getCells(): LiveData<List<Cell>> {
        if (subscription == null) {
            loadData()
        }
        return liveCells
    }
}
