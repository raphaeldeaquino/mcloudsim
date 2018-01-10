package br.ufg.inf.mcloudsim.simulator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.XMLParseException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import br.ufg.inf.mcloudsim.core.CoreEventEntity;
import br.ufg.inf.mcloudsim.core.EdgeEventEntity;
import br.ufg.inf.mcloudsim.core.EventEntity;
import br.ufg.inf.mcloudsim.core.PSBroker;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;
import br.ufg.inf.mcloudsim.network.DeployablePathNode;
import br.ufg.inf.mcloudsim.network.LegacyPathNode;
import br.ufg.inf.mcloudsim.network.PSNetworkDescriptor;
import br.ufg.inf.mcloudsim.network.PSNetworkPath;
import br.ufg.inf.mcloudsim.network.PathNode;
import br.ufg.inf.mcloudsim.network.ResourceSynthesisResult;
import br.ufg.inf.mcloudsim.network.VmTypeRegistry;
import br.ufg.inf.mcloudsim.utils.FileLoader;

public class XmlReader {

	public static PSNetworkDescriptor readNetworkDescriptor(String filename) throws XmlParserException {
		PSNetworkDescriptor networkDescriptor;
		try {
			SAXBuilder sb = new SAXBuilder();
			FileLoader fileLoader = new FileLoader(filename);
			Document doc = sb.build(fileLoader.getFile());
			networkDescriptor = new PSNetworkDescriptor();
			Element rootElement = doc.getRootElement();
			Element nodesElem = rootElement.getChild("nodes");
			List<Element> nodes = nodesElem.getChildren();
			Element pathsElem = rootElement.getChild("paths");
			List<Element> paths = pathsElem.getChildren();
			Map<String, EventEntity> nodesMap = new HashMap<>();

			for (Element nodeElem : nodes) {
				String nodeType = nodeElem.getName();
				EventEntity eventEntity = null;
				String nodeId = nodeElem.getAttributeValue("id");

				if (nodeType.equals("publisher")) {
					String topic = nodeElem.getChildText("topic");
					String rateStr = nodeElem.getChildText("rate");
					double rate = (rateStr != null && !rateStr.isEmpty()) ? Double.parseDouble(rateStr) : 0.0;
					String MIprStr = nodeElem.getChildText("MIpr");
					double MIpr = Double.parseDouble(MIprStr);
					String BtrStr = nodeElem.getChildText("Btr");
					double Btr = Double.parseDouble(BtrStr);
					eventEntity = new Publisher(nodeId, topic, rate, MIpr, Btr);
				} else if (nodeType.equals("subscriber")) {
					String topic = nodeElem.getChildText("topic");
					String tONStr = nodeElem.getChildText("tON");
					double tON = Double.parseDouble(tONStr);
					String tOFFStr = nodeElem.getChildText("tOFF");
					double tOFF = Double.parseDouble(tOFFStr);
					eventEntity = new Subscriber(nodeId, topic, tON, tOFF);
				} else if (nodeType.equals("broker")) {
					eventEntity = new PSBroker(nodeId);
				} else {
					throw new XMLParseException("Invalid node type: " + nodeType);
				}

				networkDescriptor.addNetworkNode(eventEntity);
				nodesMap.put(eventEntity.getId(), eventEntity);
			}

			for (Element pathElem : paths) {
				String pathSequenceStr = pathElem.getChildText("pathSequence");
				String[] pathSequence = pathSequenceStr.split("\\s+");
				Element nodesAttElem = pathElem.getChild("nodesAtt");
				List<Element> pathNodesElems = nodesAttElem.getChildren();
				LinkedList<DeployablePathNode> brokersPath = new LinkedList<>();
				Publisher publisher = null;
				Subscriber subscriber = null;
				for (String pathNodeId : pathSequence) {
					EventEntity eventEntity = nodesMap.get(pathNodeId);
					if (eventEntity == null)
						throw new XmlParserException("Invalid node in path = " + pathNodeId);
					PathNode pathNode = null;
					if (eventEntity instanceof EdgeEventEntity) {
						if (eventEntity instanceof Publisher)
							publisher = (Publisher) eventEntity;
						
						if (eventEntity instanceof Subscriber)
							subscriber = (Subscriber) eventEntity;
						
						Double rt = null;
						for (Element nodeElem : pathNodesElems) {
							String nodeId = nodeElem.getAttributeValue("id");
							if (nodeId.equals(pathNodeId)) {
								String rtStr = nodeElem.getAttributeValue("rt");
								rt = Double.parseDouble(rtStr);
								break;
							}
						}
						if (rt == null)
							throw new IllegalArgumentException("Response time for node " + pathNodeId + " not found");
						pathNode = new LegacyPathNode((EdgeEventEntity) eventEntity, rt);
					} else {
						Double lOth = null;
						for (Element nodeElem : pathNodesElems) {
							String nodeId = nodeElem.getAttributeValue("id");
							if (nodeId.equals(pathNodeId)) {
								String lOthStr = nodeElem.getAttributeValue("lOth");
								lOth = Double.parseDouble(lOthStr);
								break;
							}
						}
						if (lOth == null)
							throw new IllegalArgumentException("Load others for node " + pathNodeId + " not found");
						pathNode = new DeployablePathNode((CoreEventEntity)eventEntity, lOth);
						brokersPath.add((DeployablePathNode)pathNode);
					}
					
				}
				if (publisher == null || subscriber == null)
					throw new XmlParserException("Publisher and/or subscriber not found in path " + pathSequenceStr);
				
				PSNetworkPath networkPath = new PSNetworkPath(publisher, subscriber);
				networkPath.setBrokersPath(brokersPath);
				networkDescriptor.addNetworkPath(networkPath);
			}
		} catch (Exception e) {
			throw new XmlParserException(e.getMessage(), e);
		}

		return networkDescriptor;
	}

	public static List<ResourceSynthesisResult> readResourceSynthesisResults(String filename)
			throws XmlParserException {
		List<ResourceSynthesisResult> resourceSynthesisResults = new LinkedList<>();
		try {
			SAXBuilder sb = new SAXBuilder();
			FileLoader fileLoader = new FileLoader(filename);
			Document doc = sb.build(fileLoader.getFile());
			Element rootElement = doc.getRootElement();
			List<Element> vmCollectionElems = rootElement.getChild("resource").getChildren();
			VmTypeRegistry vmTypeRegistry = VmTypeRegistry.getInstance();
			Element resourceSynthesisElem = rootElement.getChild("resourceSynthesis");

			for (Element vmCollectionElem : vmCollectionElems) {
				String provider = vmCollectionElem.getAttributeValue("provider");
				String region = vmCollectionElem.getAttributeValue("region");
				for (Element vmTypesElem : vmCollectionElem.getChildren()) {
					String id = vmTypesElem.getChildText("id");
					Element cpuElem = vmTypesElem.getChild("cpu");
					String coresStr = cpuElem.getChildText("cores");
					int cores = Integer.parseInt(coresStr);
					String clockStr = cpuElem.getChildText("clock");
					double clock = Double.parseDouble(clockStr);
					String ramStr = vmTypesElem.getChildText("ram");
					double ram = Double.parseDouble(ramStr);
					String storageStr = vmTypesElem.getChildText("storage");
					double storage = Double.parseDouble(storageStr);
					String bwStr = vmTypesElem.getChildText("bandwidth");
					double bw = Double.parseDouble(bwStr);
					String priceStr = vmTypesElem.getChildText("price");
					double price = Double.parseDouble(priceStr);
					vmTypeRegistry.addVmType(id, cores, clock, ram, storage, bw, provider, region, price);
				}
			}

			for (Element synthesisResultElem : resourceSynthesisElem.getChildren()) {
				String provider = synthesisResultElem.getAttributeValue("provider");
				String region = synthesisResultElem.getAttributeValue("region");
				ResourceSynthesisResult resourceSynthesisResult = new ResourceSynthesisResult(provider, region);
				for (Element mapElem : synthesisResultElem.getChildren()) {
					String brokerId = mapElem.getAttributeValue("id");
					String vmTypeId = mapElem.getAttributeValue("value");
					resourceSynthesisResult.addResultPair(brokerId, vmTypeId);
				}
				resourceSynthesisResults.add(resourceSynthesisResult);
			}
		} catch (Exception e) {
			throw new XmlParserException(e.getMessage(), e);
		}

		return resourceSynthesisResults;
	}

	public static void main(String[] args) {
		try {
			System.out.println(readNetworkDescriptor("simulation.xml"));
			System.out.println(readResourceSynthesisResults("simulation.xml"));
		} catch (XmlParserException e) {
			e.printStackTrace();
		}
	}
}
