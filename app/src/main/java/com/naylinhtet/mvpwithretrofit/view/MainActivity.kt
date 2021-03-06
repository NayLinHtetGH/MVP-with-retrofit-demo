package com.naylinhtet.mvpwithretrofit.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.naylinhtet.mvpwithretrofit.R
import com.naylinhtet.mvpwithretrofit.adapter.RecyclerViewAdapter
import com.naylinhtet.mvpwithretrofit.contract.TestingContract
import com.naylinhtet.mvpwithretrofit.data.model.DataModel
import com.naylinhtet.mvpwithretrofit.data.response.TestingResponse
import com.naylinhtet.mvpwithretrofit.presenter.TestingPresenter
import com.naylinhtet.mvpwithretrofit.utils.NetworkUtil
import com.naylinhtet.mvpwithretrofit.utils.PaginationScrollListener
import com.naylinhtet.mvpwithretrofit.utils.ShowDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TestingContract.TestingView {

    private lateinit var presenter: TestingContract.Presenter
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var dataList: MutableList<DataModel> = ArrayList()
    private var startPage = 1
    private var lastPage = false
    private var loading = false
    private var currentPage = startPage
    private var nextPageUrl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    override fun initView() {
        presenter = TestingPresenter(this, this)
        setUpRecyclerView()
        callApi()
    }

    @Suppress("DEPRECATION")
    private fun setUpRecyclerView() {

        linearLayoutManager = LinearLayoutManager(this)
        adapter = RecyclerViewAdapter(this)
        rvTesting!!.layoutManager = linearLayoutManager
        rvTesting!!.adapter = adapter
        rvTesting!!.setHasFixedSize(true)

        rvTesting!!.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {

            override val isLastPage: Boolean
                get() = lastPage

            override val isLoading: Boolean
                get() = loading

            override fun loadMoreItems() {
                loading = true
                currentPage += 1
                callApi()
            }
        })

        swipe_refresh!!.setOnRefreshListener {
            currentPage = 1
            lastPage = false
            loading = false
            adapter.clear()
            adapter.notifyDataSetChanged()
            callApi()
            swipe_refresh!!.isRefreshing = false
            progressBar!!.visibility = View.VISIBLE
            txtError!!.visibility = View.GONE
        }

        swipe_refresh!!.setColorSchemeColors(
            resources.getColor(R.color.colorAccent)
        )
    }

    private fun callApi() {
        if (NetworkUtil.isConnected(this)) {
            progressBar!!.visibility = View.VISIBLE
            presenter.dataRequest()
        } else {
            progressBar!!.visibility = View.GONE
            ShowDialog.showEmptyDialog(
                this,
                resources.getString(R.string.str_no_internet_connection)
            )
        }
    }

    override fun setError(message: String?) {
        progressBar!!.visibility = View.GONE
        ShowDialog.showEmptyDialog(this, message.toString())
    }

    override fun testingResponse(response: TestingResponse?) {
        progressBar!!.visibility = View.GONE
        swipe_refresh!!.isRefreshing = false
        if (NetworkUtil.isConnected(this)) {
            if (currentPage == 1) {
                adapter.clear()
                adapter.notifyDataSetChanged()
            }

            if (adapter.isLoading()) {
                adapter.removeLoading()
            }
            if (loading) {
                loading = false
            }

            dataList = response!!.articles as MutableList<DataModel>
            //totalQty.text = "- " + response.meta!!.total

            if (dataList.size > 0) {
                txtError.visibility = View.GONE
                adapter.addAll(dataList)
            } else {
                txtError.text = getString(R.string.no_data)
                txtError.visibility = View.VISIBLE
            }
            //nextPageUrl = response.meta!!.has_next_page!!

            if (nextPageUrl) {
                adapter.addLoading()
            } else {
                lastPage = true
            }
        } else {
            ShowDialog.showEmptyDialog(
                this,
                resources.getString(R.string.str_no_internet_connection)
            )
        }
    }

    override fun setPresenter(presenter: TestingContract.Presenter) {
       // TODO("Not yet implemented")
    }
    //test
}