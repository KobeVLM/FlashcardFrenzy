package com.android.flashcardfrenzy.flashcard

import kotlinx.coroutines.*

class FlashcardPresenter(
    private val listView: FlashcardContract.ListView? = null,
    private val createEditView: FlashcardContract.CreateEditView? = null,
    private val repository: FlashcardRepository
) : FlashcardContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun loadCardsByDeck(deckId: Long) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.getCardsByDeck(deckId) }
                if (response.isSuccessful && response.body()?.success == true) {
                    listView?.onCardsLoaded(response.body()?.data ?: emptyList())
                } else {
                    listView?.onError(response.body()?.error?.message ?: "Failed to load cards")
                }
            } catch (e: Exception) {
                listView?.onError("Network error: ${e.message}")
            }
        }
    }

    override fun getCardById(cardId: Long) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.getCardById(cardId) }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        createEditView?.onSaveSuccess(it) // reuse to pre-fill
                    }
                } else {
                    createEditView?.onError(response.body()?.error?.message ?: "Card not found")
                }
            } catch (e: Exception) {
                createEditView?.onError("Network error: ${e.message}")
            }
        }
    }

    override fun createCard(deckId: Long, question: String, answer: String, tags: String?) {
        if (!validate(question, answer)) return
        createEditView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.createCard(deckId, FlashcardDtos.FlashcardRequest(question, answer, tags))
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { createEditView?.onSaveSuccess(it) }
                } else {
                    createEditView?.onError(response.body()?.error?.message ?: "Failed to create card")
                }
            } catch (e: Exception) {
                createEditView?.onError("Network error: ${e.message}")
            } finally {
                createEditView?.hideLoading()
            }
        }
    }

    override fun updateCard(cardId: Long, question: String, answer: String, tags: String?) {
        if (!validate(question, answer)) return
        createEditView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.updateCard(cardId, FlashcardDtos.FlashcardRequest(question, answer, tags))
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { createEditView?.onSaveSuccess(it) }
                } else {
                    createEditView?.onError(response.body()?.error?.message ?: "Failed to update card")
                }
            } catch (e: Exception) {
                createEditView?.onError("Network error: ${e.message}")
            } finally {
                createEditView?.hideLoading()
            }
        }
    }

    override fun deleteCard(cardId: Long) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.deleteCard(cardId) }
                if (response.isSuccessful && response.body()?.success == true) {
                    listView?.onCardDeleted()
                } else {
                    listView?.onError(response.body()?.error?.message ?: "Failed to delete card")
                }
            } catch (e: Exception) {
                listView?.onError("Network error: ${e.message}")
            }
        }
    }

    private fun validate(question: String, answer: String): Boolean {
        if (question.isBlank()) { createEditView?.onError("Question is required"); return false }
        if (answer.isBlank())   { createEditView?.onError("Answer is required");   return false }
        return true
    }

    override fun onDestroy() = scope.cancel()
}
