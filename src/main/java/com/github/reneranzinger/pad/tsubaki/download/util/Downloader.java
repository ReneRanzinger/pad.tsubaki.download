package com.github.reneranzinger.pad.tsubaki.download.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Utility class for the download of files from a URL to the local file system.
 *
 * @author Rene Ranzinger
 *
 */
public class Downloader
{
    private BasicCookieStore m_cookieStore = null;
    private CloseableHttpClient m_httpclient = null;

    /**
     * Creates and configures the HTTP Client used for downloading
     */
    public Downloader()
    {
        this.connect();
    }

    /**
     * Create and configures the HTTP Client.
     *
     * Method is called from the constructor or when trying to reconnect.
     */
    private void connect()
    {
        // configure timeouts
        int t_timeout = 5;
        RequestConfig t_config = RequestConfig.custom().setConnectTimeout(t_timeout * 1000)
                .setConnectionRequestTimeout(t_timeout * 1000).setSocketTimeout(t_timeout * 1000)
                .build();
        // create cookie store and HTTP client
        this.m_cookieStore = new BasicCookieStore();
        this.m_httpclient = HttpClients.custom().setDefaultCookieStore(this.m_cookieStore)
                .setDefaultRequestConfig(t_config).build();
    }

    /**
     * Close the HTTP client connection
     *
     * @throws IOException
     *             Thrown if closing fails
     */
    public void close() throws IOException
    {
        this.m_httpclient.close();
    }

    /**
     * Reconnect by closing the HTTP client and reopening it
     *
     * @throws IOException
     *             Thrown if the closing or the reopening fails
     */
    private void reconnect() throws IOException
    {
        this.close();
        this.connect();
    }

    /**
     * Download a file using the active HTTP Client and store the file in the
     * local file system
     *
     * @param a_url
     *            URL of the file to download
     * @param a_filename
     *            Name and path of the file in the local file system
     * @throws IOException
     *             If the file download or file creation fails
     */
    public void downloadFile(String a_url, String a_filename) throws IOException
    {
        CloseableHttpResponse t_response = null;
        try
        {
            // create a get request for the URL and get the response
            HttpGet t_httpGet = new HttpGet(a_url);
            t_response = this.m_httpclient.execute(t_httpGet);
        }
        catch (IOException e)
        {
            // Assume the HTTP client is stuck an reconnect
            this.reconnect();
            throw e;
        }
        // get the content of the response and copy into file
        if (t_response.getStatusLine().getStatusCode() >= 400)
        {
            throw new IOException(
                    "Download failed with status:" + t_response.getStatusLine().toString());
        }
        HttpEntity t_entity = t_response.getEntity();
        InputStream t_stream = t_entity.getContent();
        File t_targetFile = new File(a_filename);
        Files.copy(t_stream, t_targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        // clean up
        EntityUtils.consume(t_entity);
        t_response.close();
    }
}
