package com.example.JAVARest.service;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.JAVARest.dtos.BookDTO;
import com.example.JAVARest.repo.ClientRepo;

import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    @Autowired
    ClientRepo clientRepo;

    public BookDTO fetchBook(String query) {
        BookDTO bookDTO = null;

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(JacksonJaxbJsonProvider.class);

        Client client = ClientBuilder.newClient(clientConfig);
        WebTarget target = client.target(API_URL + query.replace(" ", "+"));
        Response response = target.request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {

            String jsonResponse = response.readEntity(String.class);

            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray docs = jsonObject.getJSONArray("docs");

            if (docs.length() > 0) {
                JSONObject bookJson = docs.getJSONObject(0);
                bookDTO = new BookDTO();

                bookDTO.setTitle(bookJson.optString("title"));
                bookDTO.setAuthor_name(bookJson.optJSONArray("author_name") != null
                        ? jsonToString(bookJson.getJSONArray("author_name"))
                        : null);

                JSONArray isbnArray = bookJson.optJSONArray("isbn");
                if (isbnArray != null) {
                    String isbn = findValidIsbn(isbnArray);
                    bookDTO.setIsbn(isbn);
                    bookDTO.setIsValid(isbn != null && isbn.length() == 13);
                }
                bookDTO.setIsValid(isValidISBN(bookJson.getJSONArray("isbn").toString()));
            }
        } else {
            System.out.println("Error fetching book: HTTP " + response.getStatus());
        }

        response.close();

        return bookDTO;
    }

    private String jsonToString(JSONArray jsonArray) {
        return jsonArray.length() > 0 ? jsonArray.getString(0) : null;
    }

    public boolean isValidISBN(String isbn) {
        String formattedISBN = formatISBN(isbn);

        String soapRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <IsValidISBN13 xmlns=\"http://webservices.daehosting.com/ISBN\">\n" +
                "      <sISBN>" + formattedISBN + "</sISBN>\n" +
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

    private String findValidIsbn(JSONArray isbnArray) {
        for (int i = 0; i < isbnArray.length(); i++) {
            String isbn = isbnArray.optString(i);
            if (isbn != null && isbn.length() == 13) {
                return isbn;
            }
        }
        return null;
    }

    public static String formatISBN(String isbn) {

        return isbn.substring(0, 3) + "-" +
                isbn.substring(3, 4) + "-" +
                isbn.substring(4, 8) + "-" +
                isbn.substring(8, 12) + "-" +
                isbn.substring(12);
    }

}