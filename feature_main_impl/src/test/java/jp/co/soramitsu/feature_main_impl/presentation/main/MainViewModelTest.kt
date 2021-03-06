/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.feature_main_impl.presentation.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import io.reactivex.Observable
import io.reactivex.Single
import jp.co.soramitsu.common.interfaces.WithPreloader
import jp.co.soramitsu.common.util.Event
import jp.co.soramitsu.common.util.NumbersFormatter
import jp.co.soramitsu.feature_main_api.launcher.MainRouter
import jp.co.soramitsu.feature_main_impl.domain.MainInteractor
import jp.co.soramitsu.feature_project_api.domain.model.Project
import jp.co.soramitsu.test_shared.RxSchedulersRule
import jp.co.soramitsu.test_shared.anyNonNull
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @Rule @JvmField val rule: TestRule = InstantTaskExecutorRule()
    @Rule @JvmField val schedulersRule = RxSchedulersRule()

    @Mock private lateinit var interactor: MainInteractor
    @Mock private lateinit var router: MainRouter
    @Mock private lateinit var preloader: WithPreloader
    @Mock private lateinit var numbersFormatter: NumbersFormatter

    @Mock private lateinit var formattedVotesObserver: Observer<String>
    @Mock private lateinit var showVoteDialogObserver: Observer<Event<Int>>

    private val projectPageSize = 50

    private lateinit var mainViewModel: MainViewModel

    @Before fun setUp() {
        given(interactor.getFavoriteProjects()).willReturn(Observable.just(emptyList()))
        given(interactor.getCompletedProjects()).willReturn(Observable.just(emptyList()))
        given(interactor.getVotedProjects()).willReturn(Observable.just(emptyList()))
        given(interactor.getAllProjects()).willReturn(Observable.just(emptyList()))

        given(preloader.preloadCompletableCompose()).willReturn(CompletableTransformer { upstream -> upstream })

        mainViewModel = MainViewModel(interactor, router, preloader, numbersFormatter)
    }

    @Test fun `viewModel has been initialized and projects liveData values are not empty`() {
        assertNotNull(mainViewModel.favoriteProjectsLiveData.value)
        assertNotNull(mainViewModel.completedProjectsLiveData.value)
        assertNotNull(mainViewModel.votedProjectsLiveData.value)
        assertNotNull(mainViewModel.allProjectsLiveData.value)
    }

    @Test fun `calling loadVotes function fill votesLiveData and then format it`() {
        val votes = BigDecimal(5000.0)
        val formattedVotes = "5000"
        given(interactor.getVotes(anyBoolean())).willReturn(Single.just(votes))
        given(numbersFormatter.formatInteger(votes)).willReturn(formattedVotes)
        mainViewModel.votesFormattedLiveData.observeForever(formattedVotesObserver)

        mainViewModel.loadVotes(true)

        verify(interactor).getVotes(true)
        verify(formattedVotesObserver).onChanged(eq(formattedVotes))
    }

    @Test fun `vote project flow`() {
        val votes = BigDecimal(5000.0)
        val votesToVote = 5L
        val formattedVotes = "5000"
        val votesLeftStr = "4995"
        val votesLeft = BigDecimal(votesLeftStr)
        val projectId = "test project id"
        val selectedProject = mock(Project::class.java)

        given(interactor.getVotes(anyBoolean())).willReturn(Single.just(votes))
        given(numbersFormatter.formatInteger(votes)).willReturn(formattedVotes)
        mainViewModel.votesFormattedLiveData.observeForever(formattedVotesObserver)

        mainViewModel.loadVotes(true)

        given(selectedProject.id).willReturn(projectId)
        given(interactor.voteForProject(anyString(), anyLong())).willReturn(Completable.complete())
        mainViewModel.showVoteProjectLiveData.observeForever(showVoteDialogObserver)

        mainViewModel.voteClicked(selectedProject)

        given(numbersFormatter.formatInteger(votesLeft)).willReturn(votesLeftStr)
        mainViewModel.voteForProject(votesToVote)

        verify(showVoteDialogObserver).onChanged(anyNonNull())
        verify(interactor).voteForProject(anyString(), eq(votesToVote))
        verify(numbersFormatter).formatInteger(votesLeft)
        verify(formattedVotesObserver).onChanged(eq(votesLeftStr))
    }

    @Test fun `click on project calls open details from router`() {
        val projectId = "test project id"
        val project = mock(Project::class.java)

        given(project.id).willReturn(projectId)

        mainViewModel.projectClick(project)

        verify(router).showProjectDetails(projectId)
    }

    @Test fun `onActivityCreated calls bottomView and check firebase token`() {
        given(interactor.updatePushTokenIfNeeded()).willReturn(Completable.complete())
        mainViewModel.onActivityCreated()

        verify(interactor).updatePushTokenIfNeeded()
    }

    @Test fun `check project adding to favorites`() {
        val projectId = "test project id"
        val projectToRemove = mock(Project::class.java)

        given(projectToRemove.id).willReturn(projectId)
        given(interactor.addProjectToFavorites(anyString())).willReturn(Completable.complete())

        mainViewModel.projectsFavoriteClicked(projectToRemove)

        verify(interactor).addProjectToFavorites(projectId)
    }

    @Test fun `check project removing from favorites`() {
        val projectId = "test project id"
        val projectToRemove = mock(Project::class.java)

        given(projectToRemove.isFavorite).willReturn(true)

        given(projectToRemove.id).willReturn(projectId)
        given(interactor.removeProjectFromFavorites(anyString())).willReturn(Completable.complete())

        mainViewModel.projectsFavoriteClicked(projectToRemove)

        verify(interactor).removeProjectFromFavorites(projectId)
    }

    @Test fun `click on help calls router howItWorks() function`() {
        mainViewModel.btnHelpClicked()

        verify(router).showFaq()
    }

    @Test fun `click on votes calls router showVotesScreen() function`() {
        mainViewModel.votesClick()

        verify(router).showVotesHistory()
    }

    @Test fun `check update all projects calls interactor updateAllProjects() function`() {
        given(interactor.updateAllProjects(projectPageSize)).willReturn(Single.just(3))

        mainViewModel.updateAllProjects()

        verify(interactor).updateAllProjects(projectPageSize)
    }

    @Test fun `check load more all projects calls interactor fetchRemoteAllProjects() function`() {
        given(interactor.loadMoreAllProjects(projectPageSize, 0)).willReturn(Single.just(3))

        mainViewModel.loadMoreAllProjects()

        verify(interactor).loadMoreAllProjects(projectPageSize, 0)
    }

    @Test fun `check update favorite projects calls interactor updateFavoriteProjects() function`() {
        given(interactor.updateFavoriteProjects(projectPageSize)).willReturn(Single.just(3))

        mainViewModel.updateFavoriteProjects()

        verify(interactor).updateFavoriteProjects(projectPageSize)
    }

    @Test fun `check load more favorite projects calls interactor fetchRemoteFavoriteProjects() function`() {
        given(interactor.loadMoreFavoriteProjects(projectPageSize, 0)).willReturn(Single.just(3))

        mainViewModel.loadMoreFavoriteProjects()

        verify(interactor).loadMoreFavoriteProjects(projectPageSize, 0)
    }

    @Test fun `check update voted projects calls interactor updateVotedProjects() function`() {
        given(interactor.updateVotedProjects(projectPageSize)).willReturn(Single.just(3))

        mainViewModel.updateVotedProjects()

        verify(interactor).updateVotedProjects(projectPageSize)
    }

    @Test fun `check load more voted projects calls interactor fetchRemoteVotedProjects() function`() {
        given(interactor.loadMoreVotedProjects(projectPageSize, 0)).willReturn(Single.just(3))

        mainViewModel.loadMoreVotedProjects()

        verify(interactor).loadMoreVotedProjects(projectPageSize, 0)
    }

    @Test fun `check update completed projects calls interactor updateCompletedProjects() function`() {
        given(interactor.updateCompletedProjects(projectPageSize)).willReturn(Single.just(3))

        mainViewModel.updateCompletedProjects()

        verify(interactor).updateCompletedProjects(projectPageSize)
    }

    @Test fun `check load more completed projects calls interactor fetchRemoteCompletedProjects() function`() {
        given(interactor.loadMoreCompletedProjects(projectPageSize, 0)).willReturn(Single.just(3))

        mainViewModel.loadMoreCompletedProjects()

        verify(interactor).loadMoreCompletedProjects(projectPageSize, 0)
    }
}