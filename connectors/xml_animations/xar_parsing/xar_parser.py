import math
import xml.etree.ElementTree as ET


class XARParser:

    @staticmethod
    def to_xml(xar_input_file, xml_output_file):
        xmlns_tag = 'xmlns="http://www.ald.softbankrobotics.com/schema/choregraphe/project.xsd"'

        with open(xar_input_file) as f:
            raw_data = f.read()
            if xmlns_tag in raw_data:
                raw_data = raw_data.replace(xmlns_tag, "")
            root_input = ET.fromstring(raw_data)

        robot_name = ""
        actuator_with_values = {}
        for actuators in root_input.iter('ActuatorList'):
            if not robot_name:
                robot_name = actuators.attrib.get('model').lower()
            for actuator in actuators:
                actuator_name = actuator.attrib.get('actuator')
                if actuator_name not in actuator_with_values:
                    actuator_with_values[actuator_name] = []
                values = []
                for frame in actuator:
                    values.append(math.radians(float(frame.attrib.get('value'))))
                actuator_with_values[actuator_name] += values

        root_output = ET.Element('robot', name=robot_name)
        ET.SubElement(root_output, 'emotion', {'name': 'neutral'})
        ET.SubElement(root_output, 'repeat').text = '0'
        actuators_element = ET.SubElement(root_output, 'actuators')

        for i in range(0, len(max(actuator_with_values.values(), key=len))):
            state = ET.SubElement(actuators_element, 'state')
            for actuator in actuator_with_values.keys():
                if len(actuator_with_values[actuator]) > i:
                    ET.SubElement(state, 'joint', name=actuator).text = str(actuator_with_values[actuator][i])

        ET.SubElement(root_output, 'speed').text = 'adjusted_norm'
        ET.ElementTree(root_output).write(xml_output_file)


# Example
XARParser().to_xml('input/goodbye2.xar', 'output/goodbye2.xml')
