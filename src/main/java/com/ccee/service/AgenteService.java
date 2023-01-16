package com.ccee.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ccee.domain.Agente;
import com.ccee.repository.AgenteRepository;
import com.ccee.service.projections.AgenteProjection;

@Service
public class AgenteService {
	
	@Autowired
	private AgenteRepository agenteRepository;

	public Exception lerXml(File arquivo) throws ParseException {
		Exception a = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			try {
				Document doc = db.parse(arquivo);
				doc.getDocumentElement().normalize();

				NodeList nodeList = doc.getElementsByTagName("agente");
				gravarXml(nodeList);
			} catch (SAXException e) {
				e.printStackTrace();
				a = e;
			} catch (IOException e) {
				e.printStackTrace();
				a = e;
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			a = e;
		}
		return a;
	}

	private void gravarXml(NodeList nodeList) throws ParseException {
		List<Agente> agenteList = new ArrayList<>();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = nodeList.item(i);

			Element element = (Element) node;
			
			String codigo = element.getElementsByTagName("codigo").item(0).getTextContent();
			System.out.println("Agente: " + codigo);

			String data  = element.getElementsByTagName("data").item(0).getTextContent();
			SimpleDateFormat formato2  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			Date dataFormatada = formato2.parse(data);
			String format = formato2.format(dataFormatada);
			
			System.out.println("Data: " + format);
			
			Calendar calendar = Calendar.getInstance(); 
			calendar.setTime(dataFormatada);
			
			Element e = (Element) node;
			NodeList regiaoList = e.getElementsByTagName("regiao");
			for (int j = 0; j < regiaoList.getLength(); j++) {

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String regiao = element.getElementsByTagName("regiao").item(j).getAttributes().item(0).getTextContent();
					System.out.println("regiao: " + regiao);
					
					for (int l = 0; l < 7; l++) {
						String geracao = element.getElementsByTagName("geracao").item(j).getTextContent();
						String[] geracaoSplit = geracao.trim().split("\\n");
						System.out.println("geracao: " + geracaoSplit[l]);
						
						String compra = element.getElementsByTagName("compra").item(j).getTextContent();
						String[] compraSplit = compra.trim().split("\\n");
						System.out.println("compra: " + compraSplit[l]);
						
						String precoMedio = element.getElementsByTagName("precoMedio").item(j).getTextContent();
						String[] precoMedioSplit = precoMedio.trim().split("\\n");
						System.out.println("precoMedio: " + precoMedioSplit[l]);
						
						agenteList.add(new Agente(Long.parseLong(codigo), calendar.getTime(), regiao,
								BigDecimal.valueOf(Double.parseDouble(geracaoSplit[l])), 
								BigDecimal.valueOf(Double.parseDouble(compraSplit[l])), 
								BigDecimal.valueOf(Double.parseDouble(precoMedioSplit[l]))
								));
						agenteRepository.saveAll(agenteList);
					}
				}
			}
		}
	}
	
	@Transactional(readOnly = true)
	public List<AgenteProjection> buscarRegioes() {
		return agenteRepository.findGroupByRegiao();
	}

	@Transactional(readOnly = true)
	public List<AgenteProjection> buscarPorRegiao(String regiao) {
		return agenteRepository.buscarPorRegiao(regiao);
	}
}