package com.flashcardfrenzy.flashcard

interface FlashcardContract {

    interface ListView {
        fun onCardsLoaded(cards: List<FlashcardDtos.FlashcardResponse>)
        fun onCardDeleted()
        fun onError(message: String)
    }

    interface CreateEditView {
        fun showLoading()
        fun hideLoading()
        fun onSaveSuccess(card: FlashcardDtos.FlashcardResponse)
        fun onError(message: String)
    }

    interface Presenter {
        fun loadCardsByDeck(deckId: Long)
        fun getCardById(cardId: Long)
        fun createCard(deckId: Long, question: String, answer: String, tags: String?)
        fun updateCard(cardId: Long, question: String, answer: String, tags: String?)
        fun deleteCard(cardId: Long)
        fun onDestroy()
    }
}
