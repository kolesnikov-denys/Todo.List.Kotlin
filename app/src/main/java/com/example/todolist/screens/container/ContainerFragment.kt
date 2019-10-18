package com.example.todolist.screens.container

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todolist.R
import com.example.todolist.screens.tasks.TasksContract
import com.example.todolist.screens.tasks.TasksFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_container.*


class ContainerFragment : Fragment(), ContainerContract.View,
    BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigationView.setOnNavigationItemSelectedListener(this)

        if (childFragmentManager.findFragmentByTag("ALL") == null) {
            childFragmentManager.beginTransaction()
                .add(
                    R.id.container,
                    TasksFragment(TasksContract.Storage.Filter.ALL),
                    "ALL"
                )
                .commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        showScreen(item.itemId)
        return true
    }

    override fun showScreen(id: Int) {
        val fragmentA = childFragmentManager.findFragmentByTag("ALL")
        val fragmentB = childFragmentManager.findFragmentByTag("FAVORITE")

        fragmentA?.onHiddenChanged(true)

        when (id) {
            ContainerContract.NavigationItem.ALL -> {
                if (fragmentA != null) {
                    childFragmentManager.beginTransaction().show(fragmentA).commit()
                }

                if (fragmentB != null) {
                    childFragmentManager.beginTransaction().hide(fragmentB).commit()
                }
            }

            ContainerContract.NavigationItem.FAVORITE -> {
                if (fragmentB != null) {
                    childFragmentManager.beginTransaction().show(fragmentB).commit()
                }
                if (childFragmentManager.findFragmentByTag("FAVORITE") == null) {
                    childFragmentManager.beginTransaction().add(
                        R.id.container,
                        TasksFragment(TasksContract.Storage.Filter.FAVORITE),
                        "FAVORITE"
                    )
                        .commit()
                }

                if (fragmentA != null) {
                    childFragmentManager.beginTransaction().hide(fragmentA).commit()
                }
            }
        }
    }
}
