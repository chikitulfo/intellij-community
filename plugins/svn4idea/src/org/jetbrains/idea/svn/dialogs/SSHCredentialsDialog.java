/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.svn.dialogs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.help.HelpManager;
import com.intellij.util.ui.DialogUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.svn.SvnBundle;

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 25.06.2005
 * Time: 16:47:22
 * To change this template use File | Settings | File Templates.
 */
public class SSHCredentialsDialog extends DialogWrapper implements ActionListener, DocumentListener {
  private boolean myAllowSave;
  private String myUserName;

  private String myRealm;
  private JTextField myUserNameText;
  private JCheckBox myAllowSaveCheckBox;
  private JPasswordField myPasswordText;
  private JPasswordField myPassphraseText;
  private TextFieldWithBrowseButton myKeyFileText;
  private JRadioButton myPasswordButton;
  private JRadioButton myKeyButton;
  private JLabel myPasswordLabel;
  private JLabel myKeyFileLabel;
  private JLabel myPassphraseLabel;
  private Project myProject;

  @NonNls private static final String HELP_ID = "vcs.subversion.authentication";

  protected SSHCredentialsDialog(Project project) {
    super(project, true);
    myProject = project;
    setResizable(true);
  }

  public void setup(String realm, String userName, boolean allowSave) {
    myRealm = realm;
    myUserName = userName;
    myAllowSave = allowSave;
    getHelpAction().setEnabled(true);
    init();
  }

  protected void doHelpAction() {
    HelpManager.getInstance().invokeHelp(HELP_ID);
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction(), getHelpAction()};
  }

  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    GridBagConstraints gb = new GridBagConstraints();

    // top label.
    gb.insets = new Insets(2, 2, 2, 2);
    gb.weightx = 1;
    gb.weighty = 0;
    gb.gridwidth = 3;
    gb.gridheight = 1;
    gb.gridx = 0;
    gb.gridy = 0;
    gb.anchor = GridBagConstraints.WEST;
    gb.fill = GridBagConstraints.HORIZONTAL;

    JLabel label = new JLabel(SvnBundle.message("label.ssh.authentication.realm", myRealm));
    panel.add(label, gb);

    // user name
    gb.gridy += 1;
    gb.gridwidth = 1;
    gb.weightx = 0;
    gb.fill = GridBagConstraints.NONE;

    label = new JLabel(SvnBundle.message("label.ssh.user.name"));
    panel.add(label, gb);

    // user name field
    gb.gridx = 1;
    gb.gridwidth = 2;
    gb.weightx = 1;
    gb.fill = GridBagConstraints.HORIZONTAL;

    myUserNameText = new JTextField();
    panel.add(myUserNameText, gb);
    label.setLabelFor(myUserNameText);

    if (myUserName != null) {
      myUserNameText.setText(myUserName);
    }
    myUserNameText.selectAll();
    myUserNameText.getDocument().addDocumentListener(this);

    gb.gridy += 1;
    gb.weightx = 0;
    gb.gridx = 0;
    gb.fill = GridBagConstraints.NONE;
    gb.gridwidth = 3;
    // password type
    myPasswordButton = new JRadioButton(SvnBundle.message("radio.ssh.authentication.with.password"));
    panel.add(myPasswordButton, gb);

    gb.gridy += 1;
    gb.weightx = 0;
    gb.gridx = 0;
    gb.gridwidth = 1;
    gb.fill = GridBagConstraints.NONE;
    gb.gridwidth = 1;

    myPasswordLabel = new JLabel(SvnBundle.message("label.ssh.password"));
    panel.add(myPasswordLabel, gb);

    // passworde field
    gb.gridx = 1;
    gb.weightx = 1;
    gb.gridwidth = 2;
    gb.fill = GridBagConstraints.HORIZONTAL;

    myPasswordText = new JPasswordField();
    panel.add(myPasswordText, gb);
    myPasswordLabel.setLabelFor(myPasswordText);

    gb.gridy += 1;
    gb.weightx = 0;
    gb.gridx = 0;
    gb.fill = GridBagConstraints.NONE;
    gb.gridwidth = 3;
    myKeyButton = new JRadioButton(SvnBundle.message("radio.ssh.authentication.private.key"));
    panel.add(myKeyButton, gb);

    // key file.
    gb.gridy += 1;
    gb.weightx = 0;
    gb.gridx = 0;
    gb.gridwidth = 1;
    gb.fill = GridBagConstraints.NONE;
    gb.gridwidth = 1;

    myKeyFileLabel = new JLabel(SvnBundle.message("label.ssh.key.file"));
    panel.add(myKeyFileLabel, gb);

    // key field
    gb.gridx = 1;
    gb.weightx = 1;
    gb.gridwidth = 2;
    gb.fill = GridBagConstraints.HORIZONTAL;

    myKeyFileText = new TextFieldWithBrowseButton(this);
    myKeyFileText.setEditable(false);
    panel.add(myKeyFileText, gb);
    myKeyFileLabel.setLabelFor(myKeyFileText);

    gb.gridy += 1;
    gb.weightx = 0;
    gb.gridx = 0;
    gb.gridwidth = 1;
    gb.fill = GridBagConstraints.NONE;
    gb.gridwidth = 1;

    myPassphraseLabel = new JLabel(SvnBundle.message("label.ssh.passphrase"));
    panel.add(myPassphraseLabel, gb);

    // key field
    gb.gridx = 1;
    gb.weightx = 1;
    gb.gridwidth = 2;
    gb.fill = GridBagConstraints.HORIZONTAL;

    myPassphraseText = new JPasswordField(30);
    panel.add(myPassphraseText, gb);
    myPassphraseText.getDocument().addDocumentListener(this);

    myPassphraseLabel.setLabelFor(myPassphraseText);


    ButtonGroup group = new ButtonGroup();
    group.add(myPasswordButton);
    group.add(myKeyButton);
    group.setSelected(myPasswordButton.getModel(), true);
    group.setSelected(myPasswordButton.getModel(), false);

    gb.gridy += 1;
    gb.gridx = 0;
    gb.gridwidth = 3;
    gb.weightx = 1;
    gb.anchor = GridBagConstraints.WEST;
    gb.fill = GridBagConstraints.HORIZONTAL;
    myAllowSaveCheckBox = new JCheckBox(SvnBundle.message("checkbox.ssh.keep.for.current.session"));
    panel.add(myAllowSaveCheckBox, gb);
    gb.gridy += 1;
    panel.add(new JSeparator(), gb);

    gb.gridy += 1;
    gb.weighty = 1;
    panel.add(new JLabel(), gb);

    myAllowSaveCheckBox.setSelected(!myAllowSave);
    myAllowSaveCheckBox.setEnabled(myAllowSave);

    myKeyButton.addActionListener(this);
    myPasswordButton.addActionListener(this);

    updateFields();
    updateOKButton();

    return panel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myUserNameText;
  }

  public boolean shouldCloseOnCross() {
    return true;
  }

  protected String getDimensionServiceKey() {
    return "svn.sshPasswordDialog";
  }

  public boolean isOKActionEnabled() {
    boolean ok = myUserNameText != null && myUserNameText.getText().trim().length() > 0;
    if (ok) {
      if (myPasswordButton.isSelected()) {
        ok = myPasswordText != null && myPasswordText.getPassword() != null;
      }
      else if (myKeyButton.isSelected()) {
        ok = myKeyFileText != null && myKeyFileText.getText().trim().length() > 0;
      }
    }
    return ok;
  }

  public String getUserName() {
    return isOK() && myUserNameText != null ? myUserNameText.getText() : null;
  }

  public String getKeyFile() {
    if (myKeyFileText.isEnabled()) {
      return myKeyFileText.getText();
    }
    return null;
  }

  public String getPassphrase() {
    if (myPassphraseText == null || !myPassphraseText.isEnabled()) {
      return null;
    }
    if (isOK()) {
      char[] pwd = myPassphraseText.getPassword();
      if (pwd != null) {
        return new String(pwd);
      }
    }
    return null;
  }

  public String getPassword() {
    if (myPasswordText != null && !myPasswordText.isEnabled()) {
      return null;
    }
    if (isOK() && myPasswordText != null) {
      char[] pwd = myPasswordText.getPassword();
      if (pwd != null) {
        return new String(pwd);
      }
    }
    return null;
  }

  public boolean isSaveAllowed() {
    return isOK() && myAllowSave && myAllowSaveCheckBox != null && !myAllowSaveCheckBox.isSelected();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == myPasswordButton || e.getSource() == myKeyButton) {
      updateFields();
    }
    else {
      @NonNls String path = myKeyFileText.getText();
      VirtualFile file;
      if (path != null && path.trim().length() > 0) {
        path = "file://" + path.replace(File.separatorChar, '/');
        file = VirtualFileManager.getInstance().findFileByUrl(path);
      }
      else {
        path = "file://" + System.getProperty("user.home") + "/.ssh/identity";
        path = path.replace(File.separatorChar, '/');
        file = VirtualFileManager.getInstance().findFileByUrl(path);
      }
      FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);

      descriptor.setShowFileSystemRoots(true);
      descriptor.setTitle(SvnBundle.message("dialog.title.openssh.v2.private.key"));
      descriptor.setDescription(SvnBundle.message("dialog.description.openssh.v2.private.key"));
      descriptor.setHideIgnored(false);

      String oldValue = PropertiesComponent.getInstance().getValue("FileChooser.showHiddens");
      PropertiesComponent.getInstance().setValue("FileChooser.showHiddens", Boolean.TRUE.toString());

      VirtualFile[] files = FileChooser.chooseFiles(myProject, descriptor, file);

      PropertiesComponent.getInstance().setValue("FileChooser.showHiddens", oldValue);
      if (files != null && files.length == 1) {
        path = files[0].getPath().replace('/', File.separatorChar);
        myKeyFileText.setText(path);
      }
    }
    updateOKButton();
  }

  private void updateOKButton() {
    getOKAction().setEnabled(isOKActionEnabled());
  }

  private void updateFields() {
    myPasswordText.setEnabled(myPasswordButton.isSelected());
    myPasswordLabel.setEnabled(myPasswordButton.isSelected());

    myKeyFileText.setEnabled(myKeyButton.isSelected());
    myKeyFileLabel.setEnabled(myKeyButton.isSelected());
    myPassphraseLabel.setEnabled(myKeyButton.isSelected());
    myPassphraseText.setEnabled(myKeyButton.isSelected());
  }

  public void insertUpdate(DocumentEvent e) {
    updateOKButton();
  }

  public void removeUpdate(DocumentEvent e) {
    updateOKButton();
  }

  public void changedUpdate(DocumentEvent e) {
    updateOKButton();
  }
}
