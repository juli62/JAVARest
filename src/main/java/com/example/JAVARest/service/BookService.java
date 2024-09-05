package com.example.JAVARest.service;

import org.apache.tomcat.util.digester.DocumentProperties.Charset;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.stereotype.Service;

import com.example.JAVARest.model.Book;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.StringReader;

@Service
public class BookService {

    private static final String API_URL = "http://openlibrary.org/search.json?q=";
    private static final String SOAP_URL = "http://webservices.daehosting.com/services/isbnservice.wso";
    private static final String SOAP_ACTION = "http://webservices.daehosting.com/ISBN/IsValidISBN13";

    @Transactional
    public List<Book> fetchBooks(String query) {
        List<Book> books = new ArrayList<>();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(API_URL + query.replace(" ", "+"));
        Response response = target.request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            String jsonResponse = response.readEntity(String.class);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray docs = jsonObject.getJSONArray("docs");

            for (int i = 0; i < docs.length(); i++) {
                JSONObject bookJson = docs.getJSONObject(i);
                String title = bookJson.optString("title");
                String author = bookJson.optJSONArray("author_name") != null
                        ? bookJson.optJSONArray("author_name").optString(0)
                        : null;
                JSONArray isbnArray = bookJson.optJSONArray("isbn");
                String isbn = (isbnArray != null && isbnArray.length() > 0) ? isbnArray.optString(0) : null;

                if (isbn != null && isbn.length() >= 13) {
                    isbn = isbn.substring(0, 13); // Get first 13 digits of ISBN
                }

                if (title != null) {
                    books.add(new Book(title, author, isbn));
                }
            }
        } else {
            System.out.println("Error fetching books: HTTP " + response.getStatus());
        }

        response.close();
        return books;
    }

    private boolean isValidISBN(String isbn) {
        String soapRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <IsValidISBN13 xmlns=\"http://webservices.daehosting.com/ISBN\">\n" +
                "      <sISBN>" + isbn + "</sISBN>\n" +
                "    </IsValidISBN13>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(SOAP_URL);

        Response response = target
                .request(MediaType.TEXT_XML)
                .post(Entity.entity(soapRequest, MediaType.TEXT_XML));

        boolean isValid = false;
        if (response.getStatus() == 200) {
            try {
                String responseString = response.readEntity(String.class);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(responseString)));
                document.getDocumentElement().normalize();

                // Manually find the result element
                NodeList resultNodes = document.getElementsByTagName("IsValidISBN13Result");
                if (resultNodes.getLength() > 0) {
                    Element resultElement = (Element) resultNodes.item(0);
                    String resultText = resultElement.getTextContent();
                    isValid = "true".equalsIgnoreCase(resultText);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error");
        }
        response.close();
        return isValid;
    }
}
