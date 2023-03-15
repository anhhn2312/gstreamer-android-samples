// Based on http://code.google.com/p/android-file-dialog/
//
// Copyright (c) 2011, 2012, Alexander Ponomarev <alexander.ponomarev.1@gmail.com>
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// Redistributions of source code must retain the above copyright notice, this list
// of conditions and the following disclaimer. Redistributions in binary form must
// reproduce the above copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided with the distribution.
// Neither the name of the <ORGANIZATION> nor the names of its contributors may be used
// to endorse or promote products derived from this software without specific prior
// written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
// OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
// SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
// BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
// ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
// DAMAGE.
package com.lamerman

import android.app.AlertDialog
import android.app.ListActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import org.freedesktop.gstreamer.tutorials.tutorial_5.R
import java.io.File
import java.util.*

/**
 * Activity para escolha de arquivos/diretorios.
 *
 * @author android
 */
class FileDialog : ListActivity() {
    private var path: MutableList<String>? = null
    private var myPath: TextView? = null
    private var mList: ArrayList<HashMap<String, Any?>>? = null
    private var selectButton: Button? = null
    private var parentPath: String? = null
    private var currentPath: String? = ROOT
    private var formatFilter: Array<String>? = null
    private var selectedFile: File? = null
    private val lastPositions = HashMap<String?, Int>()

    /**
     * Called when the activity is first created. Configura todos os parametros
     * de entrada e das VIEWS..
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED, intent)
        setContentView(R.layout.file_dialog_main)
        myPath = findViewById<View>(R.id.path) as TextView
        selectButton = findViewById<View>(R.id.fdButtonSelect) as Button
        selectButton!!.isEnabled = false
        selectButton!!.setOnClickListener {
            if (selectedFile != null) {
                intent.putExtra(RESULT_PATH, selectedFile!!.path)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        formatFilter = intent.getStringArrayExtra(FORMAT_FILTER)
        val cancelButton = findViewById<View>(R.id.fdButtonCancel) as Button
        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        var startPath: String?
        startPath = if (savedInstanceState != null) {
            savedInstanceState.getString("currentPath")
        } else {
            intent.getStringExtra(START_PATH)
        }
        startPath = startPath ?: ROOT
        getDir(startPath)
        val lv = findViewById<View>(android.R.id.list) as ListView
        lv.choiceMode = ListView.CHOICE_MODE_SINGLE
    }

    private fun getDir(dirPath: String?) {
        val useAutoSelection = dirPath!!.length < currentPath!!.length
        val position = lastPositions[parentPath]
        getDirImpl(dirPath)
        if (position != null && useAutoSelection) {
            listView.setSelection(position)
        }
    }

    /**
     * Monta a estrutura de arquivos e diretorios filhos do diretorio fornecido.
     *
     * @param dirPath
     * Diretorio pai.
     */
    private fun getDirImpl(dirPath: String?) {
        currentPath = dirPath
        val item: MutableList<String> = ArrayList()
        path = ArrayList()
        mList = ArrayList()
        var f = File(currentPath)
        var files = f.listFiles()
        if (files == null) {
            currentPath = ROOT
            f = File(currentPath)
            files = f.listFiles()
        }
        myPath!!.text = getText(R.string.location).toString() + ": " + currentPath
        if (currentPath != ROOT) {
            item.add(ROOT)
            addItem(ROOT, R.drawable.folder)
            path?.add(ROOT)
            item.add("../")
            addItem("../", R.drawable.folder)
            path?.add(f.parent)
            parentPath = f.parent
        }
        val dirsMap = TreeMap<String, String>()
        val dirsPathMap = TreeMap<String, String>()
        val filesMap = TreeMap<String, String>()
        val filesPathMap = TreeMap<String, String>()
        for (file in files!!) {
            if (file.isDirectory) {
                val dirName = file.name
                dirsMap[dirName] = dirName
                dirsPathMap[dirName] = file.path
            } else {
                val fileName = file.name
                val fileNameLwr = fileName.lowercase(Locale.getDefault())
                // se ha um filtro de formatos, utiliza-o
                if (formatFilter != null) {
                    var contains = false
                    for (i in formatFilter!!.indices) {
                        val formatLwr = formatFilter!![i].lowercase(Locale.getDefault())
                        if (fileNameLwr.endsWith(formatLwr)) {
                            contains = true
                            break
                        }
                    }
                    if (contains) {
                        filesMap[fileName] = fileName
                        filesPathMap[fileName] = file.path
                    }
                    // senao, adiciona todos os arquivos
                } else {
                    filesMap[fileName] = fileName
                    filesPathMap[fileName] = file.path
                }
            }
        }
        item.addAll(dirsMap.tailMap("").values)
        item.addAll(filesMap.tailMap("").values)
        path?.addAll(dirsPathMap.tailMap("").values)
        path?.addAll(filesPathMap.tailMap("").values)
        val fileList = SimpleAdapter(
            this, mList,
            R.layout.file_dialog_row, arrayOf(ITEM_KEY, ITEM_IMAGE), intArrayOf(
                R.id.fdrowtext, R.id.fdrowimage
            )
        )
        for (dir in dirsMap.tailMap("").values) {
            addItem(dir, R.drawable.folder)
        }
        for (file in filesMap.tailMap("").values) {
            addItem(file, R.drawable.file)
        }
        fileList.notifyDataSetChanged()
        listAdapter = fileList
    }

    private fun addItem(fileName: String, imageId: Int) {
        val item = HashMap<String, Any?>()
        item[ITEM_KEY] = fileName
        item[ITEM_IMAGE] = imageId
        mList!!.add(item)
    }

    /**
     * Quando clica no item da lista, deve-se: 1) Se for diretorio, abre seus
     * arquivos filhos; 2) Se puder escolher diretorio, define-o como sendo o
     * path escolhido. 3) Se for arquivo, define-o como path escolhido. 4) Ativa
     * botao de selecao.
     */
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val file = File(path!![position])
        if (file.isDirectory) {
            selectButton!!.isEnabled = false
            if (file.canRead()) {
                lastPositions[currentPath] = position
                getDir(path!![position])
            } else {
                AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setTitle(
                        "[" + file.name + "] "
                                + getText(R.string.cant_read_folder)
                    )
                    .setPositiveButton(
                        "OK"
                    ) { dialog, which -> }.show()
            }
        } else {
            if (selectedFile != null && selectedFile!!.path == file.path) {
                intent.putExtra(RESULT_PATH, selectedFile!!.path)
                setResult(RESULT_OK, intent)
                finish()
            }
            selectedFile = file
            l.setItemChecked(position, true)
            selectButton!!.isEnabled = true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            selectButton!!.isEnabled = false
            if (currentPath != ROOT) {
                getDir(parentPath)
            } else {
                return super.onKeyDown(keyCode, event)
            }
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("currentPath", currentPath)
        super.onSaveInstanceState(outState)
    }

    companion object {
        /**
         * Chave de um item da lista de paths.
         */
        private const val ITEM_KEY = "key"

        /**
         * Imagem de um item da lista de paths (diretorio ou arquivo).
         */
        private const val ITEM_IMAGE = "image"

        /**
         * Diretorio raiz.
         */
        private const val ROOT = "/"

        /**
         * Parametro de entrada da Activity: path inicial. Padrao: ROOT.
         */
        const val START_PATH = "START_PATH"

        /**
         * Parametro de entrada da Activity: filtro de formatos de arquivos. Padrao:
         * null.
         */
        const val FORMAT_FILTER = "FORMAT_FILTER"

        /**
         * Parametro de saida da Activity: path escolhido. Padrao: null.
         */
        const val RESULT_PATH = "RESULT_PATH"
    }
}