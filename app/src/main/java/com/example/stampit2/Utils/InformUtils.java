package com.example.stampit2.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.stampit2.databinding.InformOnlyInfoBinding;
import com.example.stampit2.databinding.InformYesOrNoBinding;

import java.util.function.Consumer;

public class InformUtils
{
    private static InformUtils _instance;
    private InformUtils(){}

    public static InformUtils instance()
    {
        if(_instance == null)
        {
            _instance = new InformUtils();
        }

        return _instance;
    }


    public void showOnlyInfo(Context context, String info)
    {
        InformOnlyInfoBinding binding = InformOnlyInfoBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        binding.textViewInformOnlyInfo.setText(info);
        binding.buttonInformOnlyInfo.setOnClickListener(v->{dialog.dismiss();});

        dialog.show();
    }

    public void showYesOrNo(Context context, String info, Consumer<Boolean> yesOrNoFunc)
    {
        InformYesOrNoBinding binding = InformYesOrNoBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        binding.textViewInformYesOrNo.setText(info);
        binding.buttonYesOrNoYes.setOnClickListener(v->
        {
            yesOrNoFunc.accept(true);
            dialog.dismiss();
        });
        binding.buttonYesOrNoNo.setOnClickListener(v->
        {
            yesOrNoFunc.accept(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    public void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
