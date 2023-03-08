package com.example.moviementor.other;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URL;

// Data class to hold onto the information for a single streaming provider
public class StreamingProvider {
    // First string in each provider's JSON array takes the form of "providerName - option"
    // such as the example "HBO Max - flatrate". Use this separator string to extract provider's
    // name and option from the string in the JSON array
    public static final String PROVIDER_NAME_OPTION_SEPARATOR = " - ";

    // Only three possible options that providers offer
    public static enum ProviderOption {
        BUY, RENT, STREAM
    }

    private final @NonNull String providerName;
    private final @NonNull ProviderOption providerOption;
    private final @Nullable URL providerImageUrl;

    public StreamingProvider(final @NonNull String providerName,
                             final @NonNull ProviderOption providerOption,
                             final @Nullable URL providerImageUrl) {
        this.providerName = providerName;
        this.providerOption = providerOption;
        this.providerImageUrl = providerImageUrl;
    }

    @NonNull
    public String getProviderName() {
        return this.providerName;
    }

    @NonNull
    public ProviderOption getProviderOption() {
        return this.providerOption;
    }

    @Nullable
    public URL getProviderImageUrl() {
        return this.providerImageUrl;
    }

    // Helper function that convert provider's option string into one of the three enums allowed
    @Nullable
    public static ProviderOption getProviderOption(final @NonNull String providerOptionString) {
        switch (providerOptionString) {
            case ("rent"):
                return ProviderOption.RENT;
            case ("buy"):
                return ProviderOption.BUY;
            case ("flatrate"):
                return ProviderOption.STREAM;
            default:
                return null;
        }
    }

    // Helper function that returns text for provider's content viewing option
    @NonNull
    public String getProviderOptionString() {
        if (this.providerOption == ProviderOption.BUY) {
            return "Buy";
        }
        else if (this.providerOption == ProviderOption.RENT) {
            return "Rent";
        }
        else {
            return "Stream";
        }
    }
}
