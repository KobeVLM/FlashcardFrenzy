package com.flashcardfrenzy.deck

/**
 * MVP Contract for the Deck feature.
 */
interface DeckContract {

    interface ListView {
        fun showLoading()
        fun hideLoading()
        fun onDecksLoaded(decks: List<DeckDtos.DeckResponse>)
        fun onError(message: String)
    }

    interface DetailView {
        fun showLoading()
        fun hideLoading()
        fun onDeckLoaded(deck: DeckDtos.DeckResponse)
        fun onDeckDeleted()
        fun onError(message: String)
    }

    interface CreateEditView {
        fun showLoading()
        fun hideLoading()
        fun onSaveSuccess(deck: DeckDtos.DeckResponse)
        fun onError(message: String)
    }

    interface Presenter {
        fun loadAllDecks(search: String? = null)
        fun loadMyDecks()
        fun loadDeckById(id: Long)
        fun createDeck(title: String, category: String?, description: String?)
        fun updateDeck(id: Long, title: String, category: String?, description: String?)
        fun deleteDeck(id: Long)
        fun onDestroy()
    }
}
