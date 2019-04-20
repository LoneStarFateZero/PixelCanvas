package pers.lonestar.pixelcanvas.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.CanvasInfoActivity;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.CanvasComment;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.listener.CommentInsertListener;

public class CommentDialogFragment extends DialogFragment {
    private TextView cancel;
    private TextView send;
    private EditText content;
    private BmobCanvas canvas;
    private AlertDialog dialog;
    private CommentInsertListener commentInsertListener;
    private boolean mBackCancel = false;//默认点击返回键关闭dialog
    private boolean mTouchOutsideCancel = false;//默认点击dialog外面屏幕，dialog关闭

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        final View view = LayoutInflater.from(requireActivity()).inflate(R.layout.comment_dialog, null);

        cancel = view.findViewById(R.id.comment_dialog_cancel);
        send = view.findViewById(R.id.comment_dialog_send);
        content = view.findViewById(R.id.comment_dialog_content);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送评论
                if (content.getText().toString().equals("")) {
                    Toast.makeText(requireContext(), "评论内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                PixelUser currentUser = BmobUser.getCurrentUser(PixelUser.class);
                final CanvasComment comment = new CanvasComment();
                comment.setCommentText(content.getText().toString());
                comment.setCommentUser(currentUser);
                comment.setCanvas(canvas);
                comment.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            commentInsertListener.insertComment(comment);
                            Toast.makeText(CanvasInfoActivity.getInstance(), "评论成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CanvasInfoActivity.getInstance(), "评论失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.cancel();
            }
        });

        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(mTouchOutsideCancel);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return !mBackCancel;
                }
                return false;
            }
        });
        return dialog;
    }

    public void initParameter(BmobCanvas canvas, CommentInsertListener commentInsertListener) {
        this.canvas = canvas;
        this.commentInsertListener = commentInsertListener;
    }

}
