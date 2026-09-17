package com.plyshka.medtimer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.net.toUri
import androidx.core.view.MenuCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.plyshka.medtimer.database.DatabaseManager
import com.plyshka.medtimer.database.FullMedicine
import com.plyshka.medtimer.database.MedicineRepository
import com.plyshka.medtimer.database.ReminderEvent
import com.plyshka.medtimer.database.ReminderEventRepository
import com.plyshka.medtimer.database.TagRepository
import com.plyshka.medtimer.di.Dispatcher
import com.plyshka.medtimer.di.MedTimerDispatchers
import com.plyshka.medtimer.exporters.CSVEventExport
import com.plyshka.medtimer.exporters.CSVMedicineExport
import com.plyshka.medtimer.exporters.Export
import com.plyshka.medtimer.exporters.Export.ExporterException
import com.plyshka.medtimer.exporters.PDFEventExport
import com.plyshka.medtimer.exporters.PDFMedicineExport
import com.plyshka.medtimer.helpers.EntityEditOptionsMenu
import com.plyshka.medtimer.helpers.FileHelper
import com.plyshka.medtimer.helpers.PathHelper.getExportFilename
import com.plyshka.medtimer.helpers.SimpleIdlingResource
import com.plyshka.medtimer.helpers.safeStartActivity
import com.plyshka.medtimer.medicine.tags.TagDataFromPreferences
import com.plyshka.medtimer.medicine.tags.TagsFragment
import com.plyshka.medtimer.reminders.ReminderProcessorBroadcastReceiver.Companion.requestScheduleNextNotification
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.InvocationTargetException

class OptionsMenu @AssistedInject constructor(
    @Assisted private val fragment: Fragment,
    @Assisted private val navController: NavController,
    @Assisted private val hideFilter: Boolean,
    @Assisted private val medicineViewModel: MedicineViewModel,
    val backupManagerFactory: BackupManager.Factory,
    private val pdfMedicineExportFactory: PDFMedicineExport.Factory,
    private val csvMedicineExportFactory: CSVMedicineExport.Factory,
    private val pdfEventExportFactory: PDFEventExport.Factory,
    private val csvEventExportFactory: CSVEventExport.Factory,
    private val medicineRepository: MedicineRepository,
    private val reminderEventRepository: ReminderEventRepository,
    private val tagRepository: TagRepository,
    private val databaseManager: DatabaseManager,
    private val generateTestDataFactory: GenerateTestData.Factory,
    @param:Dispatcher(MedTimerDispatchers.IO) val ioDispatcher: CoroutineDispatcher,
    @param:Dispatcher(MedTimerDispatchers.Main) val mainDispatcher: CoroutineDispatcher,
    val tagsFragmentFactory: TagsFragment.Factory
) : EntityEditOptionsMenu {
    private val context: Context = fragment.requireContext()
    private val openFileLauncher: ActivityResultLauncher<Intent>
    private val openDirectoryLauncher: ActivityResultLauncher<Intent>
    private val idlingResource: SimpleIdlingResource = SimpleIdlingResource("OptionsMenu_" + fragment.javaClass.getName())
    private lateinit var menu: Menu

    private lateinit var backupManager: BackupManager

    @AssistedFactory
    fun interface Factory {
        fun create(
            fragment: Fragment,
            navController: NavController,
            hideFilter: Boolean,
            medicineViewModel: MedicineViewModel
        ): OptionsMenu
    }

    init {
        openFileLauncher =
            fragment.registerForActivityResult(StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { this.fileSelected(it) }
                }
            }
        openDirectoryLauncher =
            fragment.registerForActivityResult(StartActivityForResult()) { result ->
                val resultData = result.data ?: return@registerForActivityResult
                if (result.resultCode == Activity.RESULT_OK) {
                    this.directorySelected(resultData.data)
                }
            }
        idlingResource.setIdle()
    }

    fun fileSelected(data: Uri) {
        fragment.lifecycleScope.launch(ioDispatcher) {
            backupManager.fileSelected(data)
        }
    }

    fun directorySelected(data: Uri?) {
        backupManager.directorySelected(data)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        enableOptionalIcons(menu)

        this.backupManager = backupManagerFactory.create(
            fragment.requireContext(),
            fragment,
            menu,
            openFileLauncher,
            openDirectoryLauncher,
            fragment.parentFragmentManager
        )
        this.menu = menu
        setupSettings()
        setupVersion()
        setupAppURL()
        setupClearEvents()
        setupExport()
        setupGenerateTestData()
        setupShowAppIntro()

        handleTagFilter()
    }

    private fun setupSettings() {
        val item = menu.findItem(R.id.settings)
        item.setOnMenuItemClickListener { _: MenuItem? ->
            try {
                navController.navigate(R.id.action_global_preferencesFragment)
                return@setOnMenuItemClickListener true
            } catch (_: IllegalStateException) {
                return@setOnMenuItemClickListener false
            }
        }
    }

    private fun setupVersion() {
        val item = menu.findItem(R.id.version)
        item.title = context.getString(R.string.version, BuildConfig.VERSION_NAME)
    }

    private fun setupAppURL() {
        val item = menu.findItem(R.id.app_url)
        item.setOnMenuItemClickListener { _: MenuItem? ->
            val myIntent = Intent(Intent.ACTION_VIEW, "https://github.com/Meister1593/medTimer".toUri())
            safeStartActivity(context, myIntent)
            true
        }
    }

    private fun setupClearEvents() {
        val item = menu.findItem(R.id.clear_events)
        item.setOnMenuItemClickListener { _ ->
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(R.string.confirm)
            builder.setMessage(R.string.are_you_sure_delete_events)
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.yes) { _, _ ->
                fragment.lifecycleScope.launch {
                    reminderEventRepository.deleteAll()
                    requestScheduleNextNotification(context)
                }
            }
            builder.setNegativeButton(R.string.cancel) { _, _ -> }
            builder.show()
            true
        }
    }

    private fun setupExport() {
        var item = menu.findItem(R.id.export_csv)
        item.setOnMenuItemClickListener { _ ->
            fragment.lifecycleScope.launch(ioDispatcher) { eventExport(true) }
            true
        }
        item = menu.findItem(R.id.export_pdf)
        item.setOnMenuItemClickListener { _ ->
            fragment.lifecycleScope.launch(ioDispatcher) { eventExport(false) }
            true
        }
        item = menu.findItem(R.id.export_medicine_csv)
        item.setOnMenuItemClickListener { _ ->
            fragment.lifecycleScope.launch(ioDispatcher) { medicineExport(true) }
            true
        }
        item = menu.findItem(R.id.export_medicine_pdf)
        item.setOnMenuItemClickListener { _ ->
            fragment.lifecycleScope.launch(ioDispatcher) { medicineExport(false) }
            true
        }
    }

    fun setupGenerateTestData() {
        val item = menu.findItem(R.id.generate_test_data)
        val itemWithEvents = menu.findItem(R.id.generate_test_data_and_events)
        if (BuildConfig.DEBUG) {
            itemWithEvents.isVisible = true
            item.isVisible = true
            val menuItemClickListener = MenuItem.OnMenuItemClickListener { menuItem: MenuItem? ->
                idlingResource.setBusy()
                fragment.lifecycleScope.launch(ioDispatcher) {
                    databaseManager.deleteAll()
                    generateTestDataFactory.create(menuItem === itemWithEvents).generateTestMedicine()
                    requestScheduleNextNotification(context)
                    idlingResource.setIdle()
                }
                true
            }
            item.setOnMenuItemClickListener(menuItemClickListener)
            itemWithEvents.setOnMenuItemClickListener(menuItemClickListener)
        } else {
            item.isVisible = false
            itemWithEvents.isVisible = false
        }
    }

    private fun setupShowAppIntro() {
        val item = menu.findItem(R.id.show_app_intro)
        if (BuildConfig.DEBUG) {
            item.isVisible = true
            item.setOnMenuItemClickListener { _ ->
                context.startActivity(Intent(context, MedTimerAppIntro::class.java))
                true
            }
        } else {
            item.isVisible = false
        }
    }

    private fun handleTagFilter() {
        if (!hideFilter) {
            fragment.lifecycleScope.launch(ioDispatcher) {
                if (tagRepository.hasAny()) {
                    try {
                        withContext(mainDispatcher) {
                            setupTagFilter()
                        }
                    } catch (_: IllegalStateException) {
                        // Intentionally empty, do nothing
                    }
                } else {
                    medicineViewModel.validTagIds.value = setOf()
                }
            }
        }
    }

    private suspend fun eventExport(isCSV: Boolean) {
        if (medicineViewModel.tagFilterActive()) {
            fragment.lifecycleScope.launch(mainDispatcher) {
                Toast.makeText(context, R.string.tag_filter_active, Toast.LENGTH_LONG).show()
            }
        }
        val reminderEvents: List<ReminderEvent> =
            medicineViewModel.filterEvents(reminderEventRepository.getAllWithoutDeletedAndAcknowledged())
        val exporter = if (isCSV) csvEventExportFactory.create(reminderEvents, fragment.getParentFragmentManager()) else pdfEventExportFactory.create(
            reminderEvents,
            fragment.getParentFragmentManager()
        )
        export(exporter)
    }

    private suspend fun medicineExport(isCSV: Boolean) {
        if (medicineViewModel.tagFilterActive()) {
            fragment.lifecycleScope.launch(mainDispatcher) {
                Toast.makeText(context, R.string.tag_filter_active, Toast.LENGTH_LONG).show()
            }
        }
        val medicines: List<FullMedicine> = medicineViewModel.filterMedicines(medicineRepository.getFullAll())
        val exporter = if (isCSV) csvMedicineExportFactory.create(medicines, fragment.getParentFragmentManager()) else pdfMedicineExportFactory.create(
            medicines,
            fragment.getParentFragmentManager()
        )
        export(exporter)
    }

    private fun setupTagFilter() {
        if (fragment.view == null) {
            return
        }

        val item = menu.findItem(R.id.tag_filter)
        item.isVisible = true
        item.setOnMenuItemClickListener { _ ->
            val tagDataFromPreferences = TagDataFromPreferences(fragment)
            val dialog: DialogFragment = tagsFragmentFactory.create(tagDataFromPreferences)
            dialog.show(fragment.getParentFragmentManager(), "tags")
            true
        }
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            medicineViewModel.validTagIds.collect { validTagIds ->
                if (validTagIds.isNullOrEmpty()) {
                    item.setIcon(R.drawable.tag)
                } else {
                    item.setIcon(R.drawable.tag_fill)
                }
            }
        }
    }

    private suspend fun export(export: Export) {
        val csvFile = File(context.cacheDir, getExportFilename(export))
        try {
            export.export(csvFile)
            FileHelper.shareFile(context, csvFile)
        } catch (_: ExporterException) {
            Toast.makeText(context, R.string.export_failed, Toast.LENGTH_LONG).show()
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onDestroy() {
        idlingResource.destroy()
    }

    companion object {
        @SuppressLint("RestrictedApi")
        fun enableOptionalIcons(menu: Menu) {
            if (menu is MenuBuilder) {
                try {
                    val m = menu.javaClass.getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean::class.java
                    )
                    m.invoke(menu, true)
                } catch (_: NoSuchMethodException) {
                    // Intentionally empty
                } catch (_: IllegalAccessException) {
                    // Intentionally empty
                } catch (_: InvocationTargetException) {
                    // Intentionally empty
                }
            }
        }
    }
}
