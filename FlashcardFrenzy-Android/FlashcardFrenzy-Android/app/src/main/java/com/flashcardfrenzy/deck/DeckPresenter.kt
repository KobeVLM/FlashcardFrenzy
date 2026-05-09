package com.flashcardfrenzy.deck

import kotlinx.coroutines.*

/**
 * MVP — Presenter
 * Coordinates between DeckRepository (Model) and deck Views.
 * One presenter handles all deck screens; only the relevant view is non-null.
 */
class DeckPresenter(
    private val listView: DeckContract.ListView? = null,
    private val detailView: DeckContract.DetailView? = null,
    private val createEditView: DeckContract.CreateEditView? = null,
    private val repository: DeckRepository
) : DeckContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // ── Load all decks (with optional search) ─────────────────────────────────

    override fun loadAllDecks(search: String?) {
        listView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.getAllDecks(search) }
                if (response.isSuccessful && response.body()?.success == true) {
                    listView?.onDecksLoaded(response.body()?.data ?: emptyList())
                } else {
                    listView?.onError(response.body()?.error?.message ?: "Failed to load decks")
                }
            } catch (e: Exception) {
                listView?.onError("Network error: ${e.message}")
            } finally {
                listView?.hideLoading()
            }
        }
    }

    // ── Load current user's own decks ─────────────────────────────────────────

    override fun loadMyDecks() {
        listView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.getMyDecks() }
                if (response.isSuccessful && response.body()?.success == true) {
                    listView?.onDecksLoaded(response.body()?.data ?: emptyList())
                } else {
                    listView?.onError(response.body()?.error?.message ?: "Failed to load your decks")
                }
            } catch (e: Exception) {
                listView?.onError("Network error: ${e.message}")
            } finally {
                listView?.hideLoading()
            }
        }
    }

    // ── Load single deck ──────────────────────────────────────────────────────

    override fun loadDeckById(id: Long) {
        detailView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.getDeckById(id) }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { detailView?.onDeckLoaded(it) }
                } else {
                    detailView?.onError(response.body()?.error?.message ?: "Deck not found")
                }
            } catch (e: Exception) {
                detailView?.onError("Network error: ${e.message}")
            } finally {
                detailView?.hideLoading()
            }
        }
    }

    // ── Create deck ───────────────────────────────────────────────────────────

    override fun createDeck(title: String, category: String?, description: String?) {
        if (!validateInput(title)) return
        createEditView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.createDeck(DeckDtos.DeckRequest(title, category, description))
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { createEditView?.onSaveSuccess(it) }
                } else {
                    createEditView?.onError(response.body()?.error?.message ?: "Failed to create deck")
                }
            } catch (e: Exception) {
                createEditView?.onError("Network error: ${e.message}")
            } finally {
                createEditView?.hideLoading()
            }
        }
    }

    // ── Update deck ───────────────────────────────────────────────────────────

    override fun updateDeck(id: Long, title: String, category: String?, description: String?) {
        if (!validateInput(title)) return
        createEditView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.updateDeck(id, DeckDtos.DeckRequest(title, category, description))
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { createEditView?.onSaveSuccess(it) }
                } else {
                    createEditView?.onError(response.body()?.error?.message ?: "Failed to update deck")
                }
            } catch (e: Exception) {
                createEditView?.onError("Network error: ${e.message}")
            } finally {
                createEditView?.hideLoading()
            }
        }
    }

    // ── Delete deck ───────────────────────────────────────────────────────────

    override fun deleteDeck(id: Long) {
        detailView?.showLoading()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { repository.deleteDeck(id) }
                if (response.isSuccessful && response.body()?.success == true) {
                    detailView?.onDeckDeleted()
                } else {
                    detailView?.onError(response.body()?.error?.message ?: "Failed to delete deck")
                }
            } catch (e: Exception) {
                detailView?.onError("Network error: ${e.message}")
            } finally {
                detailView?.hideLoading()
            }
        }
    }

    private fun validateInput(title: String): Boolean {
        if (title.isBlank()) {
            createEditView?.onError("Title is required")
            return false
        }
        return true
    }

    override fun onDestroy() = scope.cancel()
}
