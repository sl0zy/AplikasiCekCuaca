/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.cekcuaca.view;

import com.cekcuaca.model.*;
import com.cekcuaca.service.*;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import com.cekcuaca.util.*;
import javax.swing.ImageIcon;

/**
 *
 * @author slozoy
 */
public class CekCuacaFrame extends javax.swing.JFrame {

    /**
     * Creates new form CekCuacaFrame
     */
    public CekCuacaFrame() {
        initComponents();
        
        btnTambahFavorit.setEnabled(false);
        btnSimpanCSV.setEnabled(false);
        cmbFavorite.setEnabled(false);
        
        loadFavoriteCities(); // Muat favorit dari SQLite saat aplikasi dibuka
    }
    
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return "";
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 1) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase());
            } else {
                sb.append(w.toUpperCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    
    private void tampilkanCuaca(WeatherData data) {
        lblCity.setText("Kota: " + capitalizeWords(data.getCity()));
        lblTemp.setText(String.format("Suhu: %.1f °C", data.getTemperature()));
        lblHumidity.setText(String.format("Kelembapan: %.0f%%", data.getHumidity()));
        lblDescription.setText("Cuaca: " + capitalizeWords(data.getDescription()));

        ImageIcon icon = WeatherIconLoader.getIcon(data.getMain());
        if (icon != null) {
            lblIcon.setIcon(icon);
            lblIcon.setText("");
        } else {
            lblIcon.setIcon(null);
            lblIcon.setText("❌ Tidak ada ikon cuaca");
        }

        // 🔹 Tambahkan data ke tabel riwayat
        addToWeatherTable(data);

        // 🔹 Cek apakah kota sudah ada di database favorit
        try {
            java.util.List<String> favorites = com.cekcuaca.service.DatabaseService.getAllFavorites();
            boolean sudahFavorit = favorites.stream()
                    .anyMatch(fav -> fav.equalsIgnoreCase(data.getCity()));

            if (sudahFavorit) {
                btnTambahFavorit.setEnabled(false);
                System.out.println(data.getCity() + " sudah ada di favorit.");
            } else {
                btnTambahFavorit.setEnabled(true);
            }
        } catch (Exception e) {
            System.err.println("Gagal memeriksa database favorit: " + e.getMessage());
        }
    }
    
    private void addToWeatherTable(WeatherData data) {
        javax.swing.table.DefaultTableModel model = 
            (javax.swing.table.DefaultTableModel) tblWeather.getModel();

        // Pastikan tabel punya kolom
        if (model.getColumnCount() == 0) {
            model.setColumnIdentifiers(new String[]{"Kota", "Cuaca", "Suhu", "Kelembapan"});
        }

        // Tambahkan data baru
        model.addRow(new Object[]{
            capitalizeWords(data.getCity()),
            capitalizeWords(data.getDescription()),
            String.format("%.1f °C", data.getTemperature()),
            String.format("%.0f%%", data.getHumidity())
        });
        
        if (model.getRowCount() >= 16) {
            model.removeRow(0); // hapus baris paling atas
        }
        
        // 🔹 setelah data ditambahkan, cek apakah tombol CSV harus diaktifkan
        checkCsvButtonStatus();
    }
    
    private void loadFavoriteCities() {
        try {
            // Simpan dulu item pertama (default)
            Object firstItem = "Pilih Kota Favorit";

            // Hapus semua item KECUALI yang pertama
            cmbFavorite.removeAllItems();
            cmbFavorite.addItem((String) firstItem);

            // Ambil data dari database
            java.util.List<String> favorites = DatabaseService.getAllFavorites();

            if (favorites.isEmpty()) {
                cmbFavorite.setEnabled(false); // tidak ada data → disable
                System.out.println("Belum ada data favorit di database.");
            } else {
                // tambahkan data favorit setelah item default
                for (String city : favorites) {
                    cmbFavorite.addItem(city);
                }
                cmbFavorite.setEnabled(true); // ada data → enable
                System.out.println("Berhasil memuat " + favorites.size() + " kota favorit.");
            }

            // pastikan default item terpilih
            cmbFavorite.setSelectedIndex(0);

        } catch (Exception e) {
            cmbFavorite.setEnabled(false);
            cmbFavorite.removeAllItems();
            cmbFavorite.addItem("Pilih Kota Favorit");
            cmbFavorite.setSelectedIndex(0);
            System.err.println("Gagal memuat data favorit: " + e.getMessage());
        }
    }
    
    private void checkCsvButtonStatus() {
        javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel) tblWeather.getModel();

        // jika tabel punya minimal 1 baris, tombol aktif
        btnSimpanCSV.setEnabled(model.getRowCount() > 0);
    }
    
    private void saveWeatherTableToCSV() {
        javax.swing.table.DefaultTableModel model = 
            (javax.swing.table.DefaultTableModel) tblWeather.getModel();

        // Pastikan ada data
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Tidak ada data cuaca untuk disimpan.", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gunakan JFileChooser agar user bisa pilih lokasi file
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Simpan Data Cuaca ke CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("File CSV (.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            // Tambahkan ekstensi .csv jika belum ada
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".csv");
            }

            try {
                // Kumpulkan data dari JTable ke List<WeatherData>
                java.util.List<com.cekcuaca.model.WeatherData> list = new java.util.ArrayList<>();

                for (int i = 0; i < model.getRowCount(); i++) {
                    String city = model.getValueAt(i, 0).toString();
                    String description = model.getValueAt(i, 1).toString();
                    String tempStr = model.getValueAt(i, 2).toString().replace("°C", "").trim();
                    String humidityStr = model.getValueAt(i, 3).toString().replace("%", "").trim();

                    double temp = Double.parseDouble(tempStr);
                    double humidity = Double.parseDouble(humidityStr);

                    // karena di tabel kita tidak tampilkan "main", isi dengan "-"
                    list.add(new com.cekcuaca.model.WeatherData(city, "-", description, temp, humidity));
                }
                
                if (fileToSave.exists()) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "File sudah ada. Timpa file tersebut?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return; // batal menyimpan
                    }
                }
                
                // Simpan ke file CSV
                com.cekcuaca.service.FileService.saveToCSV(list, fileToSave);

                JOptionPane.showMessageDialog(this, 
                    "Data cuaca berhasil disimpan ke file:\n" + fileToSave.getAbsolutePath(), 
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Gagal menyimpan data: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void loadWeatherDataFromCSV() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Pilih File CSV untuk Dimuat");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("File CSV (.csv)", "csv"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();

            try {
                // Muat data dari file CSV
                java.util.List<com.cekcuaca.model.WeatherData> dataList =
                    com.cekcuaca.service.FileService.loadFromCSV(selectedFile);

                if (dataList.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "File tidak berisi data cuaca.", 
                        "Informasi", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Tambahkan data ke tabel
                javax.swing.table.DefaultTableModel model = 
                    (javax.swing.table.DefaultTableModel) tblWeather.getModel();

                // Jika kolom belum ada, set header dulu
                if (model.getColumnCount() == 0) {
                    model.setColumnIdentifiers(new String[]{"Kota", "Cuaca", "Suhu", "Kelembapan"});
                }

                // Kosongkan tabel sebelum memuat ulang
                model.setRowCount(0);

                // Isi tabel dengan data yang dimuat
                for (com.cekcuaca.model.WeatherData data : dataList) {
                    model.addRow(new Object[]{
                        capitalizeWords(data.getCity()),
                        capitalizeWords(data.getDescription()),
                        String.format("%.1f °C", data.getTemperature()),
                        String.format("%.0f%%", data.getHumidity())
                    });
                }

                // Aktifkan tombol Simpan CSV (karena tabel sekarang berisi data)
                checkCsvButtonStatus();

                JOptionPane.showMessageDialog(this, 
                    "Data berhasil dimuat dari file:\n" + selectedFile.getAbsolutePath(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Gagal memuat data: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                    btnSimpanCSV.setEnabled(false);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtCity = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cmbFavorite = new javax.swing.JComboBox<>();
        btnCekCuaca = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblWeather = new javax.swing.JTable();
        btnTambahFavorit = new javax.swing.JButton();
        btnSimpanCSV = new javax.swing.JButton();
        btnMuatData = new javax.swing.JButton();
        lblDescription = new javax.swing.JLabel();
        lblTemp = new javax.swing.JLabel();
        lblHumidity = new javax.swing.JLabel();
        lblCity = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Cek Cuaca Sederhana");
        setResizable(false);

        jLabel1.setText("2310010054 - Said Muhdaffa Hasyim");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel2.setText("Aplikasi Cek Cuaca Sederhana");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Masukkan Nama Kota:");

        txtCity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCityActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Kota Favorit:");

        cmbFavorite.setToolTipText("");
        cmbFavorite.setEnabled(false);
        cmbFavorite.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbFavoriteItemStateChanged(evt);
            }
        });

        btnCekCuaca.setText("Cek Cuaca");
        btnCekCuaca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekCuacaActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        tblWeather.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kota", "Cuaca", "Suhu", "Kelembapan"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblWeather);

        btnTambahFavorit.setText("Tambahkan ke Favorit");
        btnTambahFavorit.setEnabled(false);
        btnTambahFavorit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahFavoritActionPerformed(evt);
            }
        });

        btnSimpanCSV.setText("Simpan CSV");
        btnSimpanCSV.setEnabled(false);
        btnSimpanCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanCSVActionPerformed(evt);
            }
        });

        btnMuatData.setText("Muat Data");
        btnMuatData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMuatDataActionPerformed(evt);
            }
        });

        lblDescription.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblDescription.setText("Cuaca: ?");

        lblTemp.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTemp.setText("Suhu: ?");

        lblHumidity.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblHumidity.setText("Kelembapan: ?");

        lblCity.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblCity.setText("Kota: ?");

        lblIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/default.png"))); // NOI18N
        lblIcon.setIconTextGap(0);
        lblIcon.setMaximumSize(new java.awt.Dimension(200, 100));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(btnTambahFavorit)
                            .addComponent(jLabel4))
                        .addGap(59, 59, 59)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCekCuaca, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbFavorite, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(86, 86, 86))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCity)
                            .addComponent(lblDescription)
                            .addComponent(lblTemp)
                            .addComponent(lblHumidity))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnSimpanCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMuatData, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpanCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMuatData, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(31, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbFavorite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTambahFavorit, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCekCuaca, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addComponent(lblCity)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDescription)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTemp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHumidity)
                        .addGap(44, 44, 44))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 27, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(266, 266, 266))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(444, 444, 444))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahFavoritActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahFavoritActionPerformed
        // TODO add your handling code here:
        String city = lblCity.getText().replace("Kota: ", "").trim();
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kota tidak valid!");
            return;
        }

        try {
            DatabaseService.addFavorite(city);
            JOptionPane.showMessageDialog(this, city + " ditambahkan ke favorit!");
            loadFavoriteCities();
            btnTambahFavorit.setEnabled(false); // 🔹 nonaktifkan lagi setelah ditambah
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah favorit: " + e.getMessage());
        }
    }//GEN-LAST:event_btnTambahFavoritActionPerformed

    private void cmbFavoriteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbFavoriteItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String city = (String) cmbFavorite.getSelectedItem();
            if (city == null || city.equals("Pilih Kota Favorit")) {
                btnTambahFavorit.setEnabled(false);
                return; // abaikan jika masih di default
            }

            try {
                WeatherData data = WeatherService.getWeather(city);
                tampilkanCuaca(data);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal mengambil data cuaca: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_cmbFavoriteItemStateChanged

    private void btnCekCuacaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekCuacaActionPerformed
        // TODO add your handling code here:
        String city = txtCity.getText().trim();
        
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan nama kota terlebih dahulu!");
            return;
        }

        try {
            WeatherData data = WeatherService.getWeather(city);

            // reset pilihan favorit ke default
            cmbFavorite.setSelectedIndex(0);

            // tampilkan data cuaca
            tampilkanCuaca(data);

            // setelah cek cuaca, evaluasi tombol Simpan CSV
            checkCsvButtonStatus();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data cuaca: " + e.getMessage());
        }
    }//GEN-LAST:event_btnCekCuacaActionPerformed

    private void txtCityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCityActionPerformed

    private void btnSimpanCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanCSVActionPerformed
        // TODO add your handling code here:
        saveWeatherTableToCSV();
    }//GEN-LAST:event_btnSimpanCSVActionPerformed

    private void btnMuatDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMuatDataActionPerformed
        // TODO add your handling code here:
        loadWeatherDataFromCSV();
    }//GEN-LAST:event_btnMuatDataActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CekCuacaFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCekCuaca;
    private javax.swing.JButton btnMuatData;
    private javax.swing.JButton btnSimpanCSV;
    private javax.swing.JButton btnTambahFavorit;
    private javax.swing.JComboBox<String> cmbFavorite;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblCity;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblHumidity;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblTemp;
    private javax.swing.JTable tblWeather;
    private javax.swing.JTextField txtCity;
    // End of variables declaration//GEN-END:variables
}
