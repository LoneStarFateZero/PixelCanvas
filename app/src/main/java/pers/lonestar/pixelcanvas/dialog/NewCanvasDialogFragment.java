package pers.lonestar.pixelcanvas.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.PaintActivity;

public class NewCanvasDialogFragment extends DialogFragment {
    private Button create;
    private TextView canvasSizeText;
    private AlertDialog dialog;
    private int pixelCountChooser = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        final View view = LayoutInflater.from(requireActivity()).inflate(R.layout.new_dialog, null);

        create = view.findViewById(R.id.create_button);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //根据选择的Canvas大小创建画布
                //TODO
                Intent paintIntent = new Intent(requireContext(), PaintActivity.class);
                if (pixelCountChooser == 0) {
                    //未选择画布大小，重新选择
                    Toast.makeText(requireContext(), "Please Choose the Size of Canvas", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.cancel();
                    paintIntent.putExtra("pixelCount", pixelCountChooser);
                    startActivity(paintIntent);
                }
            }
        });

        canvasSizeText = view.findViewById(R.id.canvas_size_text);
        canvasSizeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupButtonClick(v);
            }
        });

        builder.setView(view);
        dialog = builder.create();
        return dialog;
    }

    private void onPopupButtonClick(final View view) {
        final PopupMenu popupMenu = new PopupMenu(requireActivity(), canvasSizeText);
        popupMenu.getMenuInflater().inflate(R.menu.canvas_size_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tiny:
                        pixelCountChooser = 16;
                        break;
                    case R.id.extra_small:
                        pixelCountChooser = 24;
                        break;
                    case R.id.small:
                        pixelCountChooser = 32;
                        break;
                    case R.id.medium:
                        pixelCountChooser = 48;
                        break;
                    case R.id.large:
                        pixelCountChooser = 64;
                        break;
                    case R.id.extra_large:
                        pixelCountChooser = 96;
                        break;
                }
                canvasSizeText.setText(item.getTitle().toString());
                popupMenu.dismiss();
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.canvas_size_menu, menu);
    }
}
